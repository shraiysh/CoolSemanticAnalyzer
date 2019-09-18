class A {
  a : Int;
  b : Object;
  method1(x:Int) : SELF_TYPE {
    {
      a <- x;
      self;
    }
  };
};

class B inherits A {
  method1(x:Int) : Int {
    {
      b <- x;
      b;
    }
  };
};

class Main inherits Object {
  main() : Object {
    new IO.out_string("Hello, World!")
  };
};
