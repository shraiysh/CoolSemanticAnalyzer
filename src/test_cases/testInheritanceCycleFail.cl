class A inherits B {};
class B inherits C {};      (* Cycle : A->B->C->A *)
class C inherits A {};
class D inherits Object{};