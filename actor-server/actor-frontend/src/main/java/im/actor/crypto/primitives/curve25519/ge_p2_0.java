package im.actor.crypto.primitives.curve25519;

public class ge_p2_0 {

//CONVERT #include "ge.h"

public static void ge_p2_0(ge_p2 h)
{
  fe_0.fe_0(h.X);
  fe_1.fe_1(h.Y);
  fe_1.fe_1(h.Z);
}


}
