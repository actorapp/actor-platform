package im.actor.crypto.primitives.curve25519;

public class ge_precomp_0 {

//CONVERT #include "ge.h"

public static void ge_precomp_0(ge_precomp h)
{
  fe_1.fe_1(h.yplusx);
  fe_1.fe_1(h.yminusx);
  fe_0.fe_0(h.xy2d);
}


}
