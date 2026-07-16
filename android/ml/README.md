# Frontend Agent model

This is the on-device "brain" of the Android app's Frontend Agent. It runs fully
on-device via TensorFlow Lite — no server round-trip — and turns a shopping
session's behavioral signals into persona/engagement/intent/navigation
predictions.

## Feature contract

Input: a float32 vector of 8 features, in this exact order, each roughly
normalized to `[0,1]`:

1. `searchRatio` = numSearchEvents / totalEvents
2. `viewRatio` = numProductViewEvents / totalEvents
3. `compareRatio` = numCompareEvents / totalEvents
4. `reviewRatio` = numReviewReadEvents / totalEvents
5. `cartRatio` = numAddToCartEvents / totalEvents
6. `sessionDurationNorm` = min(sessionDurationSeconds / 600, 1.0)
7. `avgDwellNorm` = min(avgDwellSecondsPerScreen / 60, 1.0)
8. `eventCountNorm` = min(totalEvents / 30, 1.0)

Outputs (multi-head):

- `persona` — softmax over `["Researcher", "ImpulseBuyer", "Casual"]`
- `engagement` — sigmoid scalar in `[0,1]` (app scales x100 for display)
- `purchaseIntent` — sigmoid scalar in `[0,1]` (app scales x100 for display)
- `nextScreen` — softmax over `["Home", "Search", "Product", "Cart"]`

**Important:** the TFLite converter does not preserve Keras output-layer names
on the exported tensors — they come out as generic `StatefulPartitionedCall_*`
names. The training script resolves the real head→tensor-index mapping by
running a probe sample through both the Keras model and the interpreter and
matching by shape + value, then writes that mapping to
`app/src/main/assets/agent_model_labels.json`. **Do not hardcode output
indices in Kotlin** — read them from this JSON at startup, since the mapping
can shift between retrains.

## Training data

Purely synthetic — there's no real user telemetry here. `train_agent_model.py`
generates ~6000 samples across 3 hand-specified archetypes (Researcher,
ImpulseBuyer, Casual), each archetype defining a distribution over the 8
input features plus derived engagement/intent targets and a next-screen rule,
with 10% label noise on the two classification heads so the model
generalizes rather than memorizing exact decision boundaries. This is a demo
stand-in for real behavioral data, not a claim of real-world accuracy.

## Model

Small Keras functional MLP: `Input(8) → Dense(16, relu) → Dense(12, relu)`
trunk, feeding four output heads (`persona`, `engagement`, `purchaseIntent`,
`nextScreen`). Compiled with categorical cross-entropy on the two
classification heads and MSE on the two regression heads. Trained 40 epochs,
batch size 32.

Latest run: `persona` val accuracy ≈ 0.89, `nextScreen` val accuracy ≈ 0.86,
`engagement`/`purchaseIntent` val MSE ≈ 0.003. Exported `agent_model.tflite`
is ~5.6 KB (float32, unquantized — plenty small enough to bundle as-is).

## Retraining

The environment note: this machine's default `python3` (3.14) and default
Homebrew (`/usr/local`, x86_64 under Rosetta) can't run TensorFlow — Rosetta
2 doesn't emulate the AVX instructions TF's x86_64 wheels require, and
`pyenv`'s from-source build picks up the mismatched x86_64 Homebrew libs and
fails to link for arm64. The working setup is a self-contained arm64-native
[Miniforge](https://github.com/conda-forge/miniforge) install at
`android/ml/.miniforge/` (gitignored), with a `python=3.12` env:

```bash
# one-time setup
curl -fsSL -o /tmp/miniforge.sh \
  https://github.com/conda-forge/miniforge/releases/latest/download/Miniforge3-MacOSX-arm64.sh
bash /tmp/miniforge.sh -b -p android/ml/.miniforge
android/ml/.miniforge/bin/conda create -y -p android/ml/.miniforge/envs/mlenv python=3.12
android/ml/.miniforge/envs/mlenv/bin/pip install -r android/ml/requirements.txt

# retrain
android/ml/.miniforge/envs/mlenv/bin/python android/ml/train_agent_model.py
```

This overwrites `app/src/main/assets/agent_model.tflite` and
`app/src/main/assets/agent_model_labels.json`.
