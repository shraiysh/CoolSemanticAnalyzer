class A{};
class B{};
class C inherits A{};

class Main {
    main() : Int { 5 };

    f1() : Int { {
        not "Hello";        -- Argument of 'not' has type String instead of Bool.
        ~false;             -- Argument of '~' has type Bool instead of Int.
        "Hello" + 5;        -- non-Int arguments: String + Int
        "Hello" - 5;        -- non-Int arguments: String - Int
        "Hello" / 5;        -- non-Int arguments: String / Int
        "Hello" < 5;        -- non-Int arguments: String < Int

        "Hello" = "Hello";  -- PASS
        "Hello" = self;     -- Illegal comparison with a basic type.
        self = "Hello";     -- Illegal comparison with a basic type.
        self = self;        -- PASS
        new A = self;       -- PASS
        new A = 5;          -- Illegal comparison with a basic type.
        new D;              -- 'new' used with undefined class D.
        new A = new C;      -- PASS

        -- Complex errors - demonstare recovery
        not("Hello" + 5);   -- non-Int arguments: String + Int
                            -- Argument of 'not' has type Int instead of Bool.
        5;
    } };
};