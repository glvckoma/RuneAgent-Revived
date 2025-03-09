/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.runeagent.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.silabsoft.runeagent.hook.ByteStreamMeta;
import org.silabsoft.runeagent.hook.ByteStreamMetaWrapper;

/**
 *
 * @author unsignedbyte
 */
public class ByteStreamMetaJListCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof ByteStreamMetaWrapper) {
            ByteStreamMetaWrapper wrapper = (ByteStreamMetaWrapper) value;
            setText(wrapper.getDisplayText());
            
            // Set color based on hook status (if not selected)
            if (!isSelected) {
                if (wrapper.isHooked()) {
                    setForeground(new Color(0, 150, 0)); // Green for hooked
                } else {
                    setForeground(new Color(200, 0, 0)); // Red for not hooked
                }
            }
            
            ByteStreamMeta bsm = wrapper.getMeta();
            setToolTipText(bsm.methodName());
        } else if (value instanceof ByteStreamMeta) {
            // Keep backward compatibility with ByteStreamMeta
            ByteStreamMeta bsm = (ByteStreamMeta) value;
            if (bsm.displayName() == null) {
                setText(bsm.methodName());
            } else {
                setText(bsm.displayName().equals("") ? bsm.methodName() : bsm.displayName());
            }
            setToolTipText(bsm.methodName());
        }
        
        return this;
    }
}
