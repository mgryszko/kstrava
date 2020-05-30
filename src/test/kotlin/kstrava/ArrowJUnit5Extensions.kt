package com.grysz.kstrava

import arrow.core.ForId
import arrow.core.Id
import arrow.core.extensions.id.applicative.applicative
import arrow.core.extensions.id.monad.monad
import arrow.typeclasses.Applicative
import arrow.typeclasses.Monad
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver

class IdApplicativeDependency : TypeBasedParameterResolver<Applicative<ForId>>() {
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Applicative<ForId> =
        Id.applicative()
}

class IdMonadDependency : TypeBasedParameterResolver<Monad<ForId>>() {
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Monad<ForId> =
        Id.monad()
}

