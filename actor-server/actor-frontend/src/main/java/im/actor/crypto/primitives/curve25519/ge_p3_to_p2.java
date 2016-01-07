package im.actor.crypto.primitives.curve25519;

public class ge_p3_to_p2 {

//CONVERT #include "ge.h"

/*
r = p
*/

public static void ge_p3_to_p2(ge_p2 r,ge_p3 p)
{
  fe_copy.fe_copy(r.X,p.X);
  fe_copy.fe_copy(r.Y,p.Y);
  fe_copy.fe_copy(r.Z,p.Z);
}


}
