class A {
    f1(x : Int, y : String) : Int  { 5 };
};

class B inherits A {
    f1(x : Int, y : String) : Int { 10 };
    f2() : Int { 5 };
};

class C {};

class Main {
    main() : Int { 5 };
    f2() : Int {{
        (new A).f2();                       -- Method f2(...) not a feature of class A
        (new A).f1(3);                      -- Method f1 called with wrong number of arguments.
        (new A).f1("Hello", "World");       -- Type mismatch for arg x. Formal : Int, Actual : String

        (new B)@X.f1();                     -- Static dispatch to undefined class X.
        (new B)@C.f1();                     -- Class C is not an ancestor of the caller type B
        (new B)@A.f2();                     -- Method f2(...) not a feature of class A
        (new B)@A.f1();                     -- Method f1 invoked with wrong number of arguments.
        (new B)@A.f1("Hello", "World");     -- Type mismatch for arg x. Formal : Int, Actual : String
        5;
    }};
};