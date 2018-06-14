package fr.areastudio.jwterritorio.activities;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.Territory;

public class TypeGroup extends ExpandableGroup<Address> {



    public TypeGroup(String title, List<Address> items) {
        super(title, items);

    }



}