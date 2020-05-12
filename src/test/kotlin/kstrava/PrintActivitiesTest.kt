package kstrava

import ch.tutteli.atrium.api.fluent.en_GB.isNumericallyEqualTo
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.Distance
import com.grysz.kstrava.toKm
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ActivityAdaptationTest {
    @Nested
    @DisplayName("distance")
    inner class DistanceAdaptation {
        @Test
        fun `to km`() {
            expect(Distance(meters = 124).toKm()).isNumericallyEqualTo(0.12.toBigDecimal())
            expect(Distance(meters = 125).toKm()).isNumericallyEqualTo(0.13.toBigDecimal())
            expect(Distance(meters = 8849).toKm()).isNumericallyEqualTo(8.85.toBigDecimal())
            expect(Distance(meters = 88849).toKm()).isNumericallyEqualTo(88.85.toBigDecimal())
            expect(Distance(meters = 150009).toKm()).isNumericallyEqualTo(150.01.toBigDecimal())
        }
    }
}