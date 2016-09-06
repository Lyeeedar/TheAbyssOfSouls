package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Renderables.Particle.ParticleEffect

class ParticleComponent(val particleEffect: ParticleEffect, val repeat: Boolean = false) : Component