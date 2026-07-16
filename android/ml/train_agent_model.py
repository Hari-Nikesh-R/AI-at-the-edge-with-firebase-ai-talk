"""Trains the on-device Frontend Agent model and exports it to TFLite.

Feature contract (8 float32 inputs, in this exact order):
  1. searchRatio          = numSearchEvents / totalEvents
  2. viewRatio             = numProductViewEvents / totalEvents
  3. compareRatio          = numCompareEvents / totalEvents
  4. reviewRatio           = numReviewReadEvents / totalEvents
  5. cartRatio             = numAddToCartEvents / totalEvents
  6. sessionDurationNorm   = min(sessionDurationSeconds / 600, 1.0)
  7. avgDwellNorm          = min(avgDwellSecondsPerScreen / 60, 1.0)
  8. eventCountNorm        = min(totalEvents / 30, 1.0)

Outputs (multi-head):
  persona        -> softmax over ["Researcher", "ImpulseBuyer", "Casual"]
  engagement     -> sigmoid scalar in [0,1] (app scales x100 for display)
  purchaseIntent -> sigmoid scalar in [0,1] (app scales x100 for display)
  nextScreen     -> softmax over ["Home", "Search", "Product", "Cart"]
"""

import json
import os

import numpy as np
import tensorflow as tf

np.random.seed(42)
tf.random.set_seed(42)

PERSONA_CLASSES = ["Researcher", "ImpulseBuyer", "Casual"]
SCREEN_CLASSES = ["Home", "Search", "Product", "Cart"]
FEATURE_NAMES = [
    "searchRatio",
    "viewRatio",
    "compareRatio",
    "reviewRatio",
    "cartRatio",
    "sessionDurationNorm",
    "avgDwellNorm",
    "eventCountNorm",
]

SAMPLES_PER_ARCHETYPE = 2000
LABEL_NOISE = 0.10


def clip01(x):
    return np.clip(x, 0.0, 1.0)


def maybe_relabel(label_idx, num_classes, rng):
    """Randomly reassigns a label to a different class with LABEL_NOISE probability."""
    if rng.random() < LABEL_NOISE:
        choices = [i for i in range(num_classes) if i != label_idx]
        return rng.choice(choices)
    return label_idx


def gen_researcher(n, rng):
    search = clip01(rng.normal(0.10, 0.04, n))
    view = clip01(rng.normal(0.30, 0.05, n))
    compare = clip01(rng.normal(0.28, 0.05, n))
    review = clip01(rng.normal(0.27, 0.05, n))
    cart = clip01(rng.normal(0.08, 0.04, n))
    duration = clip01(rng.normal(0.65, 0.12, n))
    dwell = clip01(rng.normal(0.62, 0.12, n))
    events = clip01(rng.normal(0.60, 0.15, n))

    engagement = clip01(0.6 + 0.3 * dwell + rng.normal(0, 0.05, n))
    intent = clip01(0.4 + 0.4 * cart + 0.2 * compare + rng.normal(0, 0.05, n))

    next_screen = np.empty(n, dtype=int)
    for i in range(n):
        if cart[i] > 0.1:
            base = SCREEN_CLASSES.index("Cart")
        elif (compare[i] + view[i]) >= max(search[i], review[i]):
            base = SCREEN_CLASSES.index("Product")
        elif search[i] >= review[i]:
            base = SCREEN_CLASSES.index("Search")
        else:
            base = SCREEN_CLASSES.index("Home")
        next_screen[i] = maybe_relabel(base, len(SCREEN_CLASSES), rng)

    persona = np.full(n, PERSONA_CLASSES.index("Researcher"))
    persona = np.array([maybe_relabel(p, len(PERSONA_CLASSES), rng) for p in persona])

    X = np.stack([search, view, compare, review, cart, duration, dwell, events], axis=1)
    return X, persona, engagement, intent, next_screen


def gen_impulse_buyer(n, rng):
    search = clip01(rng.normal(0.12, 0.05, n))
    view = clip01(rng.normal(0.20, 0.05, n))
    compare = clip01(rng.normal(0.06, 0.03, n))
    review = clip01(rng.normal(0.05, 0.03, n))
    cart = clip01(rng.normal(0.32, 0.05, n))
    duration = clip01(rng.normal(0.22, 0.08, n))
    dwell = clip01(rng.normal(0.22, 0.08, n))
    events = clip01(rng.normal(0.30, 0.10, n))

    engagement = clip01(0.3 + 0.2 * events + rng.normal(0, 0.05, n))
    intent = clip01(0.65 + 0.3 * cart + rng.normal(0, 0.05, n))

    next_screen = np.empty(n, dtype=int)
    for i in range(n):
        base = SCREEN_CLASSES.index("Cart") if cart[i] >= view[i] else SCREEN_CLASSES.index("Product")
        next_screen[i] = maybe_relabel(base, len(SCREEN_CLASSES), rng)

    persona = np.full(n, PERSONA_CLASSES.index("ImpulseBuyer"))
    persona = np.array([maybe_relabel(p, len(PERSONA_CLASSES), rng) for p in persona])

    X = np.stack([search, view, compare, review, cart, duration, dwell, events], axis=1)
    return X, persona, engagement, intent, next_screen


def gen_casual(n, rng):
    search = clip01(rng.normal(0.22, 0.05, n))
    view = clip01(rng.normal(0.22, 0.05, n))
    compare = clip01(rng.normal(0.03, 0.02, n))
    review = clip01(rng.normal(0.03, 0.02, n))
    cart = clip01(rng.normal(0.03, 0.02, n))
    duration = clip01(rng.normal(0.22, 0.10, n))
    dwell = clip01(rng.normal(0.20, 0.09, n))
    events = clip01(rng.normal(0.15, 0.08, n))

    engagement = clip01(0.15 + 0.25 * events + rng.normal(0, 0.05, n))
    intent = clip01(0.10 + 0.15 * cart + rng.normal(0, 0.04, n))

    next_screen = np.empty(n, dtype=int)
    for i in range(n):
        base = SCREEN_CLASSES.index("Search") if search[i] >= view[i] else SCREEN_CLASSES.index("Home")
        next_screen[i] = maybe_relabel(base, len(SCREEN_CLASSES), rng)

    persona = np.full(n, PERSONA_CLASSES.index("Casual"))
    persona = np.array([maybe_relabel(p, len(PERSONA_CLASSES), rng) for p in persona])

    X = np.stack([search, view, compare, review, cart, duration, dwell, events], axis=1)
    return X, persona, engagement, intent, next_screen


def build_dataset():
    rng = np.random.default_rng(42)
    parts = [
        gen_researcher(SAMPLES_PER_ARCHETYPE, rng),
        gen_impulse_buyer(SAMPLES_PER_ARCHETYPE, rng),
        gen_casual(SAMPLES_PER_ARCHETYPE, rng),
    ]
    X = np.concatenate([p[0] for p in parts], axis=0).astype(np.float32)
    persona = np.concatenate([p[1] for p in parts], axis=0)
    engagement = np.concatenate([p[2] for p in parts], axis=0).astype(np.float32).reshape(-1, 1)
    intent = np.concatenate([p[3] for p in parts], axis=0).astype(np.float32).reshape(-1, 1)
    next_screen = np.concatenate([p[4] for p in parts], axis=0)

    idx = rng.permutation(len(X))
    X, persona, engagement, intent, next_screen = (
        X[idx],
        persona[idx],
        engagement[idx],
        intent[idx],
        next_screen[idx],
    )

    persona_oh = tf.keras.utils.to_categorical(persona, num_classes=len(PERSONA_CLASSES))
    next_screen_oh = tf.keras.utils.to_categorical(next_screen, num_classes=len(SCREEN_CLASSES))

    n = len(X)
    split = int(n * 0.85)
    train = dict(
        X=X[:split],
        persona=persona_oh[:split],
        engagement=engagement[:split],
        intent=intent[:split],
        next_screen=next_screen_oh[:split],
    )
    val = dict(
        X=X[split:],
        persona=persona_oh[split:],
        engagement=engagement[split:],
        intent=intent[split:],
        next_screen=next_screen_oh[split:],
    )
    return train, val


def build_model():
    inputs = tf.keras.Input(shape=(8,), name="features")
    x = tf.keras.layers.Dense(16, activation="relu")(inputs)
    trunk = tf.keras.layers.Dense(12, activation="relu")(x)

    persona = tf.keras.layers.Dense(3, activation="softmax", name="persona")(trunk)
    engagement = tf.keras.layers.Dense(1, activation="sigmoid", name="engagement")(trunk)
    purchase_intent = tf.keras.layers.Dense(1, activation="sigmoid", name="purchaseIntent")(trunk)
    next_screen = tf.keras.layers.Dense(4, activation="softmax", name="nextScreen")(trunk)

    model = tf.keras.Model(
        inputs=inputs,
        outputs=[persona, engagement, purchase_intent, next_screen],
    )
    model.compile(
        optimizer="adam",
        loss={
            "persona": "categorical_crossentropy",
            "engagement": "mse",
            "purchaseIntent": "mse",
            "nextScreen": "categorical_crossentropy",
        },
        metrics={
            "persona": "accuracy",
            "nextScreen": "accuracy",
        },
    )
    return model


def main():
    train, val = build_dataset()
    model = build_model()
    model.summary()

    history = model.fit(
        train["X"],
        {
            "persona": train["persona"],
            "engagement": train["engagement"],
            "purchaseIntent": train["intent"],
            "nextScreen": train["next_screen"],
        },
        validation_data=(
            val["X"],
            {
                "persona": val["persona"],
                "engagement": val["engagement"],
                "purchaseIntent": val["intent"],
                "nextScreen": val["next_screen"],
            },
        ),
        epochs=40,
        batch_size=32,
        verbose=2,
    )

    final = {k: v[-1] for k, v in history.history.items()}
    print("\n=== Final metrics ===")
    for k, v in final.items():
        print(f"{k}: {v:.4f}")

    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    assets_dir = os.path.join(repo_root, "app", "src", "main", "assets")
    os.makedirs(assets_dir, exist_ok=True)
    tflite_path = os.path.join(assets_dir, "agent_model.tflite")

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    with open(tflite_path, "wb") as f:
        f.write(tflite_model)

    size_kb = os.path.getsize(tflite_path) / 1024
    print(f"\nSaved TFLite model to {tflite_path} ({size_kb:.1f} KB)")

    interpreter = tf.lite.Interpreter(model_path=tflite_path)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print("\n=== TFLite input details ===")
    for d in input_details:
        print(d)

    print("\n=== TFLite output details ===")
    for d in output_details:
        print(d)

    # The TFLite converter does not preserve Keras output-layer names on the
    # exported tensors (they come out as generic StatefulPartitionedCall_*
    # names), so identify each head by running one sample through both the
    # Keras model and the interpreter and matching by shape + value.
    probe = train["X"][:1]
    keras_persona, keras_engagement, keras_intent, keras_next_screen = model.predict(probe, verbose=0)

    interpreter.set_tensor(input_details[0]["index"], probe.astype(np.float32))
    interpreter.invoke()
    tfl_outputs = {d["index"]: interpreter.get_tensor(d["index"]) for d in output_details}

    def index_matching(shape_len, ref_value=None):
        candidates = [d["index"] for d in output_details if d["shape"][-1] == shape_len]
        if ref_value is None or len(candidates) == 1:
            return candidates[0]
        best_idx, best_diff = None, None
        for idx in candidates:
            diff = float(np.abs(tfl_outputs[idx] - ref_value).sum())
            if best_diff is None or diff < best_diff:
                best_idx, best_diff = idx, diff
        return best_idx

    persona_idx = index_matching(3)
    next_screen_idx = index_matching(4)
    scalar_candidates = [d["index"] for d in output_details if d["shape"][-1] == 1]
    engagement_idx = index_matching(1, ref_value=keras_engagement)
    remaining_scalar = [i for i in scalar_candidates if i != engagement_idx]
    purchase_intent_idx = remaining_scalar[0] if remaining_scalar else engagement_idx

    output_index_map = {
        "persona": int(persona_idx),
        "engagement": int(engagement_idx),
        "purchaseIntent": int(purchase_intent_idx),
        "nextScreen": int(next_screen_idx),
    }
    print("\n=== Output head -> TFLite output index (matched by shape + probe value) ===")
    print(json.dumps(output_index_map, indent=2))
    print("Keras probe outputs -> persona:", keras_persona, "engagement:", keras_engagement,
          "purchaseIntent:", keras_intent, "nextScreen:", keras_next_screen)
    print("TFLite probe outputs by index:", {k: v.tolist() for k, v in tfl_outputs.items()})

    labels_path = os.path.join(assets_dir, "agent_model_labels.json")
    labels_doc = {
        "featureNames": FEATURE_NAMES,
        "personaClasses": PERSONA_CLASSES,
        "nextScreenClasses": SCREEN_CLASSES,
        "outputIndexMap": output_index_map,
        "inputDetails": [
            {"name": d["name"], "index": d["index"], "shape": d["shape"].tolist()}
            for d in input_details
        ],
        "outputDetails": [
            {"name": d["name"], "index": d["index"], "shape": d["shape"].tolist()}
            for d in output_details
        ],
    }
    with open(labels_path, "w") as f:
        json.dump(labels_doc, f, indent=2)
    print(f"\nSaved labels/index map to {labels_path}")


if __name__ == "__main__":
    main()
