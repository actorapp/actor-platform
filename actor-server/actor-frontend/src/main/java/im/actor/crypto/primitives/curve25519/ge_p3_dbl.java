package im.actor.crypto.primitives.curve25519;

public class ge_p3_dbl {

//CONVERT #include "ge.h"

/*
r = 2 * p
*/

public static void ge_p3_dbl(ge_p1p1 r,ge_p3 p)
{
  ge_p2 q = new ge_p2();
  ge_p3_to_p2.ge_p3_to_p2(q,p);
  ge_p2_dbl.ge_p2_dbl(r,q);
}


}
