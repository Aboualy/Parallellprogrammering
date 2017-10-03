package com.startwork.streetguide;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.util.HashMap;


public class AutoSearchView extends AutoCompleteTextView {

    public AutoSearchView(Context context, AttributeSet attributeSet) {

        super(context, attributeSet);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {

        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get("description");
    }
}
