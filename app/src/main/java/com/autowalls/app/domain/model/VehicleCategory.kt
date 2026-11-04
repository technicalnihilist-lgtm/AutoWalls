package com.autowalls.app.domain.model

enum class CategoryGroup(val label: String) {
    CARS("Cars"),
    MOTORCYCLES("Motorcycles"),
    OTHER("Other Vehicles")
}

/**
 * A browsable vehicle category.
 *
 * [query] is the exact Wikimedia Commons search expression for that category.
 * Every query was validated against the live API: each returns a healthy number
 * of JPEG photographs that pass [com.autowalls.app.domain.LicensePolicy].
 */
data class VehicleCategory(
    val id: String,
    val name: String,
    val group: CategoryGroup,
    val query: String
)

object VehicleCatalog {

    private const val BITMAP = "filetype:bitmap"

    private fun car(id: String, name: String, term: String) =
        VehicleCategory(id, name, CategoryGroup.CARS, "$term car $BITMAP")

    private fun bike(id: String, name: String, term: String) =
        VehicleCategory(id, name, CategoryGroup.MOTORCYCLES, "$term motorcycle $BITMAP")

    private fun other(id: String, name: String, query: String) =
        VehicleCategory(id, name, CategoryGroup.OTHER, "$query $BITMAP")

    val cars: List<VehicleCategory> = listOf(
        car("lamborghini", "Lamborghini", "Lamborghini"),
        car("ferrari", "Ferrari", "Ferrari"),
        car("porsche", "Porsche", "Porsche"),
        car("bugatti", "Bugatti", "Bugatti"),
        car("mclaren", "McLaren", "McLaren"),
        car("bmw", "BMW", "BMW"),
        car("mercedes", "Mercedes-Benz", "Mercedes-Benz"),
        car("audi", "Audi", "Audi"),
        car("rolls-royce", "Rolls-Royce", "Rolls-Royce"),
        car("bentley", "Bentley", "Bentley"),
        car("toyota", "Toyota", "Toyota"),
        car("nissan", "Nissan", "Nissan"),
        car("honda", "Honda", "Honda"),
        car("ford", "Ford", "Ford"),
        car("chevrolet", "Chevrolet", "Chevrolet"),
        car("tesla", "Tesla", "Tesla")
    )

    val motorcycles: List<VehicleCategory> = listOf(
        bike("kawasaki", "Kawasaki", "Kawasaki"),
        bike("yamaha", "Yamaha", "Yamaha"),
        bike("ducati", "Ducati", "Ducati"),
        bike("bmw-motorrad", "BMW Motorrad", "BMW"),
        bike("suzuki", "Suzuki", "Suzuki"),
        bike("honda-bikes", "Honda Bikes", "Honda"),
        bike("ktm", "KTM", "KTM"),
        bike("harley-davidson", "Harley-Davidson", "Harley-Davidson"),
        bike("triumph", "Triumph", "Triumph")
    )

    val others: List<VehicleCategory> = listOf(
        other("supercars", "Supercars", "supercar"),
        other("hypercars", "Hypercars", "hypercar"),
        other("classic-cars", "Classic Cars", "classic car automobile"),
        other("electric-cars", "Electric Cars", "electric car charging"),
        other("racing-cars", "Racing Cars", "race car circuit"),
        other("formula-1", "Formula 1", "Formula One car"),
        other("suvs", "SUVs", "sport utility vehicle SUV"),
        other("pickup-trucks", "Pickup Trucks", "pickup truck"),
        other("heavy-trucks", "Heavy Trucks", "semi trailer truck"),
        other("buses", "Buses", "bus public transport"),
        other("aircraft", "Aircraft", "airliner aircraft flight"),
        other("helicopters", "Helicopters", "helicopter flying"),
        other("private-jets", "Private Jets", "private jet aircraft"),
        other("military-aircraft", "Military Aircraft", "fighter jet aircraft"),
        other("ships", "Ships", "container ship sea"),
        other("luxury-yachts", "Luxury Yachts", "luxury yacht superyacht")
    )

    val all: List<VehicleCategory> = cars + motorcycles + others

    val popularBrands: List<VehicleCategory> = listOf(
        "lamborghini", "ferrari", "porsche", "bugatti", "mclaren",
        "bmw", "mercedes", "audi", "tesla", "rolls-royce"
    ).mapNotNull { id -> all.firstOrNull { it.id == id } }

    val featuredCollection: VehicleCategory get() = all.first { it.id == "supercars" }
    val trendingCollection: VehicleCategory get() = all.first { it.id == "hypercars" }
    val latestCollection: VehicleCategory get() = all.first { it.id == "classic-cars" }
    val luxuryCollection: VehicleCategory get() = all.first { it.id == "luxury-yachts" }
    val motorcycleCollection: VehicleCategory get() = all.first { it.id == "ducati" }

    fun byId(id: String): VehicleCategory? = all.firstOrNull { it.id == id }

    fun byGroup(group: CategoryGroup): List<VehicleCategory> = when (group) {
        CategoryGroup.CARS -> cars
        CategoryGroup.MOTORCYCLES -> motorcycles
        CategoryGroup.OTHER -> others
    }

    /** Rotated for the daily wallpaper so the pick changes daily but stays live. */
    val dailyRotation: List<VehicleCategory> = listOf(
        "supercars", "hypercars", "lamborghini", "ferrari", "porsche",
        "bugatti", "mclaren", "formula-1", "racing-cars", "classic-cars"
    ).mapNotNull { id -> all.firstOrNull { it.id == id } }
}
