/*
 * ByteStreamMetaWrapper - Wrapper class for ByteStreamMeta that includes hook status
 */
package org.silabsoft.runeagent.hook;

/**
 * Wrapper class for ByteStreamMeta that includes hook status
 * 
 * @author Cline
 */
public class ByteStreamMetaWrapper {
    private final ByteStreamMeta meta;
    private final boolean hooked;
    
    public ByteStreamMetaWrapper(ByteStreamMeta meta, boolean hooked) {
        this.meta = meta;
        this.hooked = hooked;
    }
    
    public ByteStreamMeta getMeta() {
        return meta;
    }
    
    public boolean isHooked() {
        return hooked;
    }
    
    public String getDisplayText() {
        String name = meta.displayName().isEmpty() ? meta.methodName() : meta.displayName();
        return name + " => " + (hooked ? "hooked" : "not hooked");
    }
}
