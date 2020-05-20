package kstrava.table

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.Distance
import com.grysz.kstrava.Gear
import com.grysz.kstrava.distanceRenderer
import com.grysz.kstrava.gearIdRenderer
import com.grysz.kstrava.gearNameRenderer
import com.grysz.kstrava.startDateRenderer
import kstrava.anyActivity
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PrintActivitiesTest {
    @Test
    fun `distance renderer`() {
        with(distanceRenderer) {
            expect(anyActivity.copy(distance = Distance(meters = 124)).show()).toBe("0.12")
            expect(anyActivity.copy(distance = Distance(meters = 125)).show()).toBe("0.13")
            expect(anyActivity.copy(distance = Distance(meters = 8849)).show()).toBe("8.85")
            expect(anyActivity.copy(distance = Distance(meters = 88849)).show()).toBe("88.85")
            expect(anyActivity.copy(distance = Distance(meters = 150009)).show()).toBe("150.01")
        }
    }

    @Test
    fun `start date renderer`() {
        with(startDateRenderer) {
            expect(anyActivity.copy(startDate = LocalDateTime.of(2020, 1, 2, 3, 4, 5)).show()).toBe("2020-01-02 03:04")
        }
    }

    @Test
    fun `gear renderers`() {
        val activity = anyActivity.copy(gear = Gear("::id::", "::name::"))
        with(gearIdRenderer) {
            expect(activity.show()).toBe("::id::")
        }
        with(gearNameRenderer) {
            expect(activity.show()).toBe("::name::")
        }
    }

    @Test
    fun `null gear renderers`() {
        val activity = anyActivity.copy(gear = null)
        with(gearIdRenderer) {
            expect(activity.show()).toBe("none")
        }
        with(gearNameRenderer) {
            expect(activity.show()).toBe("")
        }
    }
}