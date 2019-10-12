class A {
  fun() : Int {
    5
  };
};

class B inherits A{
};

class C inherits B{
};

class Main{

  x : C <- new C;

  main() : Object {
    x@B.fun()
  };
};