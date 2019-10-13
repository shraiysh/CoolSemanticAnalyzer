
class Foo inherits Bazz{};
class Bar inherits Foo{};
class Razz inherits Bar{};
class Bazz {};

class A {
    g : Foo  <- case self of
        n : Bazz => (new Foo);
        n : Razz => (new Bar);
        n : Foo  => (new Razz);
        n : Bar => n;
    esac;

    g1 : Foo  <- case self of
        n       : Bazz => (new Foo);
        self    : Razz => (new Bar);        -- ERROR - Cannot bind to self
        n       : ABC  => (new Razz);       -- ERROR - Invalid type
        n       : Bazz => n;                -- ERROR - Two cases should not be same type
    esac;
};

class Main {
    main() : Int {5};
};