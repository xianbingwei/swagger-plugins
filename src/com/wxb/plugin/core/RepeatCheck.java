package com.wxb.plugin.core;

import com.intellij.psi.impl.source.PsiClassReferenceType;

public class RepeatCheck {

    PsiClassReferenceType node;

    RepeatCheck parent;

    public RepeatCheck(PsiClassReferenceType node, RepeatCheck parent) {
        this.node = node;
        this.parent = parent;
    }

    public boolean isCycle() {
        return isCycle(this.node, parent);
    }

    public boolean isCycle(PsiClassReferenceType node, RepeatCheck parent) {
        if (node == null ||
                parent == null || parent.node == null) {
            return false;
        }
        if (parent.node == node) {
            return true;
        }
        return isCycle(node, parent.parent);
    }
}
