class Foo inherits Bazz{};
class Bar inherits Foo{};
class Razz inherits Bar{};
class Bazz {};
-- Razz < Bar < Foo < Bazz
class A {
    g : Foo  <- case self of
        n : Bazz => {n;(new Foo);};         -- This should not be an error.
        n : Razz => (new Bar);
        n : Foo  => (new Razz);
        n : Bar => n;
    esac;

};

class Main {
    a : A <- new A;
    main() : Int {5};
};