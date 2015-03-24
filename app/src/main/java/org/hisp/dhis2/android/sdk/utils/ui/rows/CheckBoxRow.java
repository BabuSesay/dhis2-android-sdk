/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.sdk.utils.ui.rows;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import org.hisp.dhis2.android.sdk.R;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis2.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;

public class CheckBoxRow implements Row {
    private LayoutInflater inflater;
    private BaseValue dataValue;
    private boolean editable = true;
    private CheckBoxHolder holder;
    private String label;
    
    public CheckBoxRow(LayoutInflater inflater, String label, BaseValue dataValue) {
        this.inflater = inflater;
        this.label = label;
        this.dataValue = dataValue;
    }

    @Override
    public View getView(View convertView) {
        View view;
        
        if (convertView == null) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.listview_row_checkbox, null);
            TextView textLabel = (TextView) rootView.findViewById(R.id.text_label);
            CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.checkbox);
            CheckBoxListener listener = new CheckBoxListener(dataValue);
            
            checkBox.setOnCheckedChangeListener(listener);
            holder = new CheckBoxHolder(textLabel, checkBox, listener);
            
            rootView.setTag(holder);
            view = rootView;
        } else {
            view = convertView;
            holder = (CheckBoxHolder) view.getTag();
        }
        
        holder.textLabel.setText(label);
        holder.listener.setField(dataValue);
        
        if (dataValue.value.equals(BaseValue.TRUE)) holder.checkBox.setChecked(true);
        else if (dataValue.value.equals(BaseValue.EMPTY_VALUE)) holder.checkBox.setChecked(false);
        setEditable(editable);
        
        return view;
    }

    @Override
    public TextView getEntryView() {
        return null;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
        if(holder!=null) {
            if(editable) {
                holder.checkBox.setEnabled(true);
            } else {
                holder.checkBox.setEnabled(false);
            }
        }
    }

    private class CheckBoxListener implements OnCheckedChangeListener {
        private BaseValue dataValue;
        
        CheckBoxListener(BaseValue dataValue) {
            this.dataValue = dataValue;
        }
        
        void setField(BaseValue dataValue) {
            this.dataValue = dataValue;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                dataValue.value = BaseValue.TRUE ;
            } else {
                dataValue.value = BaseValue.EMPTY_VALUE;
            }
        }
        
    }

    private class CheckBoxHolder {
        final TextView textLabel;
        final CheckBox checkBox;
        final CheckBoxListener listener;
        
        CheckBoxHolder(TextView textLabel, CheckBox checkBox, CheckBoxListener listener) {
            this.textLabel = textLabel;
            this.checkBox = checkBox;
            this.listener = listener;
        }
    }   
}


