class Main {
    main() : Int {5};
    f1() : Int {"Hello"};      -- ERROR - Return type does not conform
    f2() : A { "Hello" };      -- ERROR - Return type undefined
};