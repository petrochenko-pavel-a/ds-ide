package com.onpositive.musket.data.images;

import java.util.ArrayList;
import java.util.Set;

import com.onpositive.musket.data.table.ITabularItem;

public class MultiClassInstanceSegmentationItem extends MultiClassSegmentationItem{

	public MultiClassInstanceSegmentationItem(String id, MultiClassSegmentationDataSet binarySegmentationDataSet,
			ArrayList<ITabularItem> items) {
		super(id, binarySegmentationDataSet, items);
	}
	

	protected boolean fits(IMask m, Set<Object> ownerClasses) {
		boolean focus = false;
		Object focusObj = owner.getSettings().get(MultiClassSegmentationDataSet.FOCUS_ON_TARGET_CLASS);
		if(focusObj instanceof Boolean) {
			focus = (boolean) focusObj;
		}
		else if(focusObj instanceof String) {
			try {
				focus = Boolean.parseBoolean((String) focusObj);
			}
			catch (Exception e) {}
		}
        return !focus || ownerClasses.contains(m.clazz());
	}

}
