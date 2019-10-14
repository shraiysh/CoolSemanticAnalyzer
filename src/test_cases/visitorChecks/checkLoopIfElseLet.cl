class Main {
    main() : Int { 5 };

    x : String;

    f1() : Int {{
        if 5 then 3 else 4 fi;                  -- If condition does not have type Bool.
        x <- if true then "Hello" else 5 fi;    -- Type Object (String + Int) of assigned expression does
                                                -- not conform to declared type String of identifier x.
        while 5 loop 3 pool;                    -- Loop condition does not have type Bool.
        x <- while false loop 3 pool;           -- Type Object (loop) of assigned expression does
                                                -- not conform to declared type String of identifier x.

        let self : Int <- 2 in x;               -- 'self' cannot be bound in a let expression.
        let x : ABC in x;                       -- 'let' used with undefined class ABC
        let x : Int <- "Hello" in x;            -- Type String of assigned expression does
                                                -- not conform to declared type Int of identifier x.
        x <- let x : Int <- 3 in ("Hello");     -- PASS
        let y : Int, y : String in y;           -- PASS (ALLOWED)
        5;
    }};
};