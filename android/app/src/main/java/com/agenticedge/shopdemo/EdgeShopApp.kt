package com.agenticedge.shopdemo

import android.app.Application
import com.agenticedge.shopdemo.agent.AgentRepository

class EdgeShopApp : Application() {

    lateinit var agentRepository: AgentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        agentRepository = AgentRepository.getInstance(this)
    }
}
