(*No Error*)

class Foo {
    y : Int;
};

class Main inherits Foo {
  x : Int;
  self : Int;                   -- attr cannot have name `self'
  x : Int;                      -- attr `x' has been redefined
  x : String;			-- attr `x' has been redefined
  y : Int;                      -- attr `y' has been redefined
  main() : Int { 5 };
};
