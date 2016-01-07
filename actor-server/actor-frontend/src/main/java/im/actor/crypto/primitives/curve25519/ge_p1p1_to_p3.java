package im.actor.crypto.primitives.curve25519;

public class ge_p1p1_to_p3 {

//CONVERT #include "ge.h"

/*
r = p
*/

public static void ge_p1p1_to_p3(ge_p3 r,ge_p1p1 p)
{
  fe_mul.fe_mul(r.X,p.X,p.T);
  fe_mul.fe_mul(r.Y,p.Y,p.Z);
  fe_mul.fe_mul(r.Z,p.Z,p.T);
  fe_mul.fe_mul(r.T,p.X,p.Y);
}


}
