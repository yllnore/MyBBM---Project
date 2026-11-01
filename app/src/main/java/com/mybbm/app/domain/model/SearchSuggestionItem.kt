package com.mybbm.app.domain.model

/**
 * Model representasi item saran autocomplete polimorfik
 * yang menampung saran lokasi (Nominatim), kendaraan (Room/Supabase), atau jenis BBM.
 */
sealed class SearchSuggestionItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String

    data class Location(
        val searchResult: LocationSearchResult
    ) : SearchSuggestionItem() {
        override val id: String get() = "loc_${searchResult.lat}_${searchResult.lon}_${searchResult.title}"
        override val title: String get() = searchResult.title
        override val subtitle: String get() = searchResult.subtitle
    }

    data class Vehicle(
        val vehicle: VehicleModel
    ) : SearchSuggestionItem() {
        override val id: String get() = "veh_${vehicle.id}"
        override val title: String get() = vehicle.name
        override val subtitle: String get() = "${vehicle.type} · ${vehicle.fuelEfficiency} km/L"
    }

    data class Fuel(
        val fuel: FuelModel
    ) : SearchSuggestionItem() {
        override val id: String get() = "fuel_${fuel.id}"
        override val title: String get() = fuel.name
        override val subtitle: String get() = "Rp ${fuel.pricePerLiter} / Liter"
    }
}
