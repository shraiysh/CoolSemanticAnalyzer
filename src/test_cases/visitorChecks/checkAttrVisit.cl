class A {};
class B inherits A {};

class Main {
    main() : Int { 5 };

    x : XYZ;            -- ERROR - Undefined type id -> Object
    y : ABC <- self;    -- ERROR - Undefined type id -> Object

    z : Int <- "Hello"; -- ERROR - Types do not conform
    a : A <- new B;     -- Types conform
    b : B <- new A;     -- ERROR - Types do not conform

};