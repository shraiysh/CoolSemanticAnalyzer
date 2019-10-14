class Main {
    main() : Int { 5 };
    x : Int;
    f1() : Int {{
        x <- "Hello";       -- Type String of assigned expression does
                            -- not conform to declared type Int of identifier x.
        self <- self;       -- Cannot assign to 'self'.
        y <- 5;             -- Assignment to undeclared variable y
        x <- 5;             -- PASS
        5;

    }};
};