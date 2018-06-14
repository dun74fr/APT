package fr.areastudio.jwterritorio.activities;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.Territory;

public class MapGroup extends ExpandableGroup<Address> {

    private final Territory territory;

    public MapGroup(Territory territory, List<Address> items) {
        super(territory.name, items);
        this.territory = territory;
    }

    public Territory getTerritory() {
        return territory;
    }

}