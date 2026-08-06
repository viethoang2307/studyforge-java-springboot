package com.studyforge.banking;

/** A small reference showing Java's four access levels. */
public class MemberVisibilityExample {
    private final String privateMember = "only this class";
    final String packagePrivateMember = "classes in this package";
    protected final String protectedMember = "package and subclasses";
    public final String publicMember = "all callers";

    public final String describePrivateMember() {
        return privateMember;
    }
}
