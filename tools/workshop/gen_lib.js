"use strict";
const document={querySelector(){return null}};
const MOD_MODELS = {"crocodile":{"tex":[64,128],"parts":[{"name":"body","parent":null,"pivot":[0.0,19.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-5.0,-3.0,-10.0],"s":[10.0,6.0,20.0],"uv":[0,0],"inflate":0.0,"mirror":false}]},{"name":"head","parent":"body","pivot":[0.0,-1.0,-10.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-3.5,-2.5,-6.0],"s":[7.0,5.0,6.0],"uv":[28,42],"inflate":0.0,"mirror":false},{"o":[-2.5,-1.0,-15.0],"s":[5.0,3.0,9.0],"uv":[0,42],"inflate":0.0,"mirror":false},{"o":[-3.0,-3.5,-4.0],"s":[2.0,1.0,2.0],"uv":[36,64],"inflate":0.0,"mirror":false},{"o":[1.0,-3.5,-4.0],"s":[2.0,1.0,2.0],"uv":[44,64],"inflate":0.0,"mirror":false}]},{"name":"jaw","parent":"head","pivot":[0.0,2.0,-6.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.5,0.0,-9.0],"s":[5.0,1.0,9.0],"uv":[0,54],"inflate":0.0,"mirror":false}]},{"name":"leg_fl","parent":null,"pivot":[-5.0,21.0,-7.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.0,0.0,-1.5],"s":[3.0,3.0,3.0],"uv":[48,54],"inflate":0.0,"mirror":false}]},{"name":"leg_fr","parent":null,"pivot":[5.0,21.0,-7.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.5],"s":[3.0,3.0,3.0],"uv":[0,64],"inflate":0.0,"mirror":false}]},{"name":"leg_bl","parent":null,"pivot":[-5.0,21.0,7.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.0,0.0,-1.5],"s":[3.0,3.0,3.0],"uv":[12,64],"inflate":0.0,"mirror":false}]},{"name":"leg_br","parent":null,"pivot":[5.0,21.0,7.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.5],"s":[3.0,3.0,3.0],"uv":[24,64],"inflate":0.0,"mirror":false}]},{"name":"tail1","parent":"body","pivot":[0.0,0.0,10.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-3.0,-2.0,0.0],"s":[6.0,4.0,12.0],"uv":[0,26],"inflate":0.0,"mirror":false}]},{"name":"tail2","parent":"tail1","pivot":[0.0,0.5,12.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.0,-1.5,0.0],"s":[4.0,3.0,10.0],"uv":[36,26],"inflate":0.0,"mirror":false}]},{"name":"tail3","parent":"tail2","pivot":[0.0,0.5,10.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,-1.0,0.0],"s":[2.0,2.0,8.0],"uv":[28,54],"inflate":0.0,"mirror":false}]}]},"shark":{"tex":[64,128],"parts":[{"name":"body","parent":null,"pivot":[0.0,18.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-4.0,-4.0,-10.0],"s":[8.0,8.0,20.0],"uv":[0,0],"inflate":0.0,"mirror":false},{"o":[-0.5,-11.0,-2.0],"s":[1.0,7.0,6.0],"uv":[0,47],"inflate":0.0,"mirror":false}]},{"name":"head","parent":"body","pivot":[0.0,0.0,-10.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-3.5,-3.5,-7.0],"s":[7.0,7.0,7.0],"uv":[10,28],"inflate":0.0,"mirror":false},{"o":[-2.5,-2.0,-11.0],"s":[5.0,4.0,4.0],"uv":[32,47],"inflate":0.0,"mirror":false}]},{"name":"fin_l","parent":"body","pivot":[-4.0,2.0,-3.0],"rot":[0.0,0.0,28.648],"cubes":[{"o":[-7.0,0.0,-1.0],"s":[7.0,1.0,5.0],"uv":[0,60],"inflate":0.0,"mirror":false}]},{"name":"fin_r","parent":"body","pivot":[4.0,2.0,-3.0],"rot":[0.0,0.0,-28.648],"cubes":[{"o":[0.0,0.0,-1.0],"s":[7.0,1.0,5.0],"uv":[24,60],"inflate":0.0,"mirror":false}]},{"name":"tail1","parent":"body","pivot":[0.0,0.0,10.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.5,-3.0,0.0],"s":[5.0,6.0,8.0],"uv":[38,28],"inflate":0.0,"mirror":false}]},{"name":"tail2","parent":"tail1","pivot":[0.0,0.0,8.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.5,-2.0,0.0],"s":[3.0,4.0,6.0],"uv":[14,47],"inflate":0.0,"mirror":false}]},{"name":"tail_fin","parent":"tail2","pivot":[0.0,0.0,5.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-0.5,-8.0,0.0],"s":[1.0,15.0,4.0],"uv":[0,28],"inflate":0.0,"mirror":false}]}]},"jaguar":{"tex":[64,64],"parts":[{"name":"body","parent":null,"pivot":[0.0,14.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-3.0,-3.0,-8.0],"s":[6.0,6.0,16.0],"uv":[0,0],"inflate":0.0,"mirror":false}]},{"name":"head","parent":null,"pivot":[0.0,12.0,-8.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.5,-2.5,-5.0],"s":[5.0,5.0,5.0],"uv":[44,0],"inflate":0.0,"mirror":false},{"o":[-1.5,0.0,-6.5],"s":[3.0,2.0,2.0],"uv":[0,31],"inflate":0.0,"mirror":false},{"o":[-2.5,-3.5,-2.0],"s":[1.0,1.0,1.0],"uv":[10,31],"inflate":0.0,"mirror":false},{"o":[1.5,-3.5,-2.0],"s":[1.0,1.0,1.0],"uv":[14,31],"inflate":0.0,"mirror":false}]},{"name":"leg_fl","parent":null,"pivot":[-2.0,17.0,-6.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,7.0,2.0],"uv":[0,22],"inflate":0.0,"mirror":false}]},{"name":"leg_fr","parent":null,"pivot":[2.0,17.0,-6.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,7.0,2.0],"uv":[8,22],"inflate":0.0,"mirror":false}]},{"name":"leg_bl","parent":null,"pivot":[-2.0,17.0,6.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,7.0,2.0],"uv":[16,22],"inflate":0.0,"mirror":false}]},{"name":"leg_br","parent":null,"pivot":[2.0,17.0,6.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,7.0,2.0],"uv":[24,22],"inflate":0.0,"mirror":false}]},{"name":"tail1","parent":null,"pivot":[0.0,12.0,8.0],"rot":[51.566,0.0,0.0],"cubes":[{"o":[-0.5,0.0,0.0],"s":[1.0,1.0,8.0],"uv":[32,22],"inflate":0.0,"mirror":false}]},{"name":"tail2","parent":"tail1","pivot":[0.0,0.0,8.0],"rot":[-34.377,0.0,0.0],"cubes":[{"o":[-0.5,0.0,0.0],"s":[1.0,1.0,6.0],"uv":[50,22],"inflate":0.0,"mirror":false}]}]},"boar":{"tex":[64,64],"parts":[{"name":"body","parent":null,"pivot":[0.0,15.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-4.5,-4.0,-7.0],"s":[9.0,8.0,14.0],"uv":[0,0],"inflate":0.0,"mirror":false},{"o":[-1.0,-5.0,-6.0],"s":[2.0,1.0,10.0],"uv":[26,22],"inflate":0.0,"mirror":false}]},{"name":"head","parent":null,"pivot":[0.0,14.0,-7.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-3.5,-3.0,-6.0],"s":[7.0,6.0,6.0],"uv":[0,22],"inflate":0.0,"mirror":false},{"o":[-2.0,0.0,-8.0],"s":[4.0,3.0,2.0],"uv":[36,34],"inflate":0.0,"mirror":false},{"o":[-2.5,1.0,-8.0],"s":[1.0,2.0,1.0],"uv":[52,34],"inflate":0.0,"mirror":false},{"o":[1.5,1.0,-8.0],"s":[1.0,2.0,1.0],"uv":[56,34],"inflate":0.0,"mirror":false},{"o":[-3.5,-4.0,-3.0],"s":[2.0,1.0,1.0],"uv":[0,42],"inflate":0.0,"mirror":false},{"o":[1.5,-4.0,-3.0],"s":[2.0,1.0,1.0],"uv":[6,42],"inflate":0.0,"mirror":false}]},{"name":"leg_fl","parent":null,"pivot":[-3.0,19.0,-5.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.5,0.0,-1.5],"s":[3.0,5.0,3.0],"uv":[50,22],"inflate":0.0,"mirror":false}]},{"name":"leg_fr","parent":null,"pivot":[3.0,19.0,-5.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.5,0.0,-1.5],"s":[3.0,5.0,3.0],"uv":[0,34],"inflate":0.0,"mirror":false}]},{"name":"leg_bl","parent":null,"pivot":[-3.0,19.0,5.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.5,0.0,-1.5],"s":[3.0,5.0,3.0],"uv":[12,34],"inflate":0.0,"mirror":false}]},{"name":"leg_br","parent":null,"pivot":[3.0,19.0,5.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.5,0.0,-1.5],"s":[3.0,5.0,3.0],"uv":[24,34],"inflate":0.0,"mirror":false}]},{"name":"tail1","parent":null,"pivot":[0.0,12.0,7.0],"rot":[22.918,0.0,0.0],"cubes":[{"o":[-0.5,0.0,0.0],"s":[1.0,4.0,1.0],"uv":[48,34],"inflate":0.0,"mirror":false}]}]},"crab":{"tex":[64,32],"parts":[{"name":"body","parent":null,"pivot":[0.0,21.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-4.0,-1.5,-3.0],"s":[8.0,3.0,6.0],"uv":[0,0],"inflate":0.0,"mirror":false},{"o":[-2.5,-3.5,-3.0],"s":[1.0,2.0,1.0],"uv":[52,0],"inflate":0.0,"mirror":false},{"o":[1.5,-3.5,-3.0],"s":[1.0,2.0,1.0],"uv":[56,0],"inflate":0.0,"mirror":false}]},{"name":"claw_l","parent":null,"pivot":[-4.0,21.0,-3.0],"rot":[0.0,22.918,0.0],"cubes":[{"o":[-3.0,-1.5,-3.0],"s":[3.0,3.0,3.0],"uv":[28,0],"inflate":0.0,"mirror":false}]},{"name":"claw_r","parent":null,"pivot":[4.0,21.0,-3.0],"rot":[0.0,-22.918,0.0],"cubes":[{"o":[0.0,-1.5,-3.0],"s":[3.0,3.0,3.0],"uv":[40,0],"inflate":0.0,"mirror":false}]},{"name":"leg_fl","parent":null,"pivot":[-4.0,21.5,-1.0],"rot":[0.0,0.0,-34.377],"cubes":[{"o":[-4.0,-0.5,-0.5],"s":[4.0,1.0,1.0],"uv":[0,9],"inflate":0.0,"mirror":false}]},{"name":"leg_ml","parent":null,"pivot":[-4.0,21.5,1.0],"rot":[0.0,0.0,-34.377],"cubes":[{"o":[-4.0,-0.5,-0.5],"s":[4.0,1.0,1.0],"uv":[10,9],"inflate":0.0,"mirror":false}]},{"name":"leg_bl","parent":null,"pivot":[-4.0,21.5,2.5],"rot":[0.0,0.0,-34.377],"cubes":[{"o":[-4.0,-0.5,-0.5],"s":[4.0,1.0,1.0],"uv":[20,9],"inflate":0.0,"mirror":false}]},{"name":"leg_fr","parent":null,"pivot":[4.0,21.5,-1.0],"rot":[0.0,0.0,34.377],"cubes":[{"o":[0.0,-0.5,-0.5],"s":[4.0,1.0,1.0],"uv":[30,9],"inflate":0.0,"mirror":false}]},{"name":"leg_mr","parent":null,"pivot":[4.0,21.5,1.0],"rot":[0.0,0.0,34.377],"cubes":[{"o":[0.0,-0.5,-0.5],"s":[4.0,1.0,1.0],"uv":[40,9],"inflate":0.0,"mirror":false}]},{"name":"leg_br","parent":null,"pivot":[4.0,21.5,2.5],"rot":[0.0,0.0,34.377],"cubes":[{"o":[0.0,-0.5,-0.5],"s":[4.0,1.0,1.0],"uv":[50,9],"inflate":0.0,"mirror":false}]}]},"tentacle":{"tex":[64,64],"parts":[{"name":"tent1","parent":null,"pivot":[0.0,24.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-4.0,-8.0,-4.0],"s":[8.0,8.0,8.0],"uv":[0,0],"inflate":0.0,"mirror":false}]},{"name":"tent2","parent":"tent1","pivot":[0.0,-8.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-3.5,-8.0,-3.5],"s":[7.0,8.0,7.0],"uv":[0,16],"inflate":0.0,"mirror":false}]},{"name":"tent3","parent":"tent2","pivot":[0.0,-8.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-3.0,-8.0,-3.0],"s":[6.0,8.0,6.0],"uv":[32,0],"inflate":0.0,"mirror":false}]},{"name":"tent4","parent":"tent3","pivot":[0.0,-8.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.5,-8.0,-2.5],"s":[5.0,8.0,5.0],"uv":[32,16],"inflate":0.0,"mirror":false}]},{"name":"tent5","parent":"tent4","pivot":[0.0,-8.0,0.0],"rot":[14.324,0.0,0.0],"cubes":[{"o":[-2.0,-8.0,-2.0],"s":[4.0,8.0,4.0],"uv":[0,32],"inflate":0.0,"mirror":false}]},{"name":"tent6","parent":"tent5","pivot":[0.0,-8.0,0.0],"rot":[28.648,0.0,0.0],"cubes":[{"o":[-1.5,-8.0,-1.5],"s":[3.0,8.0,3.0],"uv":[20,32],"inflate":0.0,"mirror":false}]}]},"snake":{"tex":[64,32],"parts":[{"name":"head","parent":null,"pivot":[0.0,23.0,-8.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.5,-1.0,-4.0],"s":[3.0,2.0,4.0],"uv":[0,0],"inflate":0.0,"mirror":false}]},{"name":"seg1","parent":null,"pivot":[0.0,23.0,-8.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,-1.0,0.0],"s":[2.0,2.0,4.0],"uv":[14,0],"inflate":0.0,"mirror":false}]},{"name":"seg2","parent":"seg1","pivot":[0.0,0.0,4.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,-1.0,0.0],"s":[2.0,2.0,4.0],"uv":[26,0],"inflate":0.0,"mirror":false}]},{"name":"seg3","parent":"seg2","pivot":[0.0,0.0,4.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,-1.0,0.0],"s":[2.0,2.0,4.0],"uv":[38,0],"inflate":0.0,"mirror":false}]},{"name":"seg4","parent":"seg3","pivot":[0.0,0.0,4.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,-1.0,0.0],"s":[2.0,2.0,4.0],"uv":[50,0],"inflate":0.0,"mirror":false}]},{"name":"seg5","parent":"seg4","pivot":[0.0,0.0,4.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-0.5,-0.5,0.0],"s":[1.0,1.0,4.0],"uv":[0,6],"inflate":0.0,"mirror":false}]},{"name":"seg6","parent":"seg5","pivot":[0.0,0.0,4.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-0.5,-0.5,0.0],"s":[1.0,1.0,3.0],"uv":[10,6],"inflate":0.0,"mirror":false}]}]},"monkey":{"tex":[64,32],"parts":[{"name":"body","parent":null,"pivot":[0.0,13.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.5,0.0,-1.5],"s":[5.0,6.0,3.0],"uv":[20,0],"inflate":0.0,"mirror":false}]},{"name":"head","parent":null,"pivot":[0.0,13.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-2.5,-5.0,-2.5],"s":[5.0,5.0,5.0],"uv":[0,0],"inflate":0.0,"mirror":false},{"o":[-1.5,-2.5,-3.5],"s":[3.0,2.0,1.0],"uv":[32,10],"inflate":0.0,"mirror":false},{"o":[-3.5,-4.0,-0.5],"s":[1.0,2.0,1.0],"uv":[40,10],"inflate":0.0,"mirror":false},{"o":[2.5,-4.0,-0.5],"s":[1.0,2.0,1.0],"uv":[44,10],"inflate":0.0,"mirror":false}]},{"name":"arm_l","parent":null,"pivot":[-3.5,13.5,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,7.0,2.0],"uv":[36,0],"inflate":0.0,"mirror":false}]},{"name":"arm_r","parent":null,"pivot":[3.5,13.5,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,7.0,2.0],"uv":[44,0],"inflate":0.0,"mirror":false}]},{"name":"leg_fl","parent":null,"pivot":[-1.5,19.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,5.0,2.0],"uv":[52,0],"inflate":0.0,"mirror":false}]},{"name":"leg_fr","parent":null,"pivot":[1.5,19.0,0.0],"rot":[0.0,0.0,0.0],"cubes":[{"o":[-1.0,0.0,-1.0],"s":[2.0,5.0,2.0],"uv":[0,10],"inflate":0.0,"mirror":false}]},{"name":"tail1","parent":null,"pivot":[0.0,18.0,1.5],"rot":[-51.566,0.0,0.0],"cubes":[{"o":[-0.5,-0.5,0.0],"s":[1.0,1.0,6.0],"uv":[8,10],"inflate":0.0,"mirror":false}]},{"name":"tail2","parent":"tail1","pivot":[0.0,0.0,6.0],"rot":[-45.837,0.0,0.0],"cubes":[{"o":[-0.5,-0.5,0.0],"s":[1.0,1.0,4.0],"uv":[22,10],"inflate":0.0,"mirror":false}]}]},"ship":{"tex":[256,512],"parts":[{"name":"keel","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-10.0,0.0,-26.0],"s":[20.0,3.0,52.0],"uv":[0,0],"inflate":0.0,"mirror":false}]},{"name":"side_left","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-12.0,-10.0,-26.0],"s":[2.0,13.0,52.0],"uv":[144,0],"inflate":0.0,"mirror":false}]},{"name":"side_right","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[10.0,-10.0,-26.0],"s":[2.0,13.0,52.0],"uv":[0,65],"inflate":0.0,"mirror":false}]},{"name":"stern","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-12.0,-10.0,-28.0],"s":[24.0,13.0,2.0],"uv":[108,65],"inflate":0.0,"mirror":false}]},{"name":"bow","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-8.0,-10.0,26.0],"s":[16.0,13.0,6.0],"uv":[160,65],"inflate":0.0,"mirror":false}]},{"name":"bow_tip","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-4.0,-9.0,32.0],"s":[8.0,11.0,5.0],"uv":[204,65],"inflate":0.0,"mirror":false}]},{"name":"deck","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-10.0,-4.0,-26.0],"s":[20.0,1.0,52.0],"uv":[0,130],"inflate":0.0,"mirror":false}]},{"name":"rail_left","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-12.0,-12.0,-26.0],"s":[2.0,2.0,52.0],"uv":[144,130],"inflate":0.0,"mirror":false}]},{"name":"rail_right","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[10.0,-12.0,-26.0],"s":[2.0,2.0,52.0],"uv":[0,184],"inflate":0.0,"mirror":false}]},{"name":"cabin","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-9.0,-12.0,-26.0],"s":[18.0,8.0,10.0],"uv":[108,184],"inflate":0.0,"mirror":false}]},{"name":"cabin_roof","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-10.0,-13.0,-27.0],"s":[20.0,1.0,12.0],"uv":[164,184],"inflate":0.0,"mirror":false}]},{"name":"mast","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-1.0,-80.0,4.0],"s":[2.0,76.0,2.0],"uv":[228,184],"inflate":0.0,"mirror":false}]},{"name":"yard","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-20.0,-68.0,3.0],"s":[40.0,2.0,2.0],"uv":[0,262],"inflate":0.0,"mirror":false}]},{"name":"bowsprit","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-1.0,-12.0,36.0],"s":[2.0,2.0,16.0],"uv":[84,262],"inflate":0.0,"mirror":false}]},{"name":"flag","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[0.0,-80.0,-6.0],"s":[1.0,6.0,10.0],"uv":[120,262],"inflate":0.0,"mirror":false}]},{"name":"cannon_l1","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-14.0,-8.0,-10.0],"s":[2.0,2.0,4.0],"uv":[142,262],"inflate":0.0,"mirror":false}]},{"name":"cannon_l2","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[-14.0,-8.0,8.0],"s":[2.0,2.0,4.0],"uv":[154,262],"inflate":0.0,"mirror":false}]},{"name":"cannon_r1","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[12.0,-8.0,-10.0],"s":[2.0,2.0,4.0],"uv":[166,262],"inflate":0.0,"mirror":false}]},{"name":"cannon_r2","parent":null,"pivot":[0,0,0],"rot":[0,0,0],"cubes":[{"o":[12.0,-8.0,8.0],"s":[2.0,2.0,4.0],"uv":[178,262],"inflate":0.0,"mirror":false}]},{"name":"sail","parent":null,"pivot":[0.0,-66.0,0.0],"rot":[0,0,0],"cubes":[{"o":[-18.0,0.0,6.0],"s":[36.0,40.0,1.0],"uv":[0,280],"inflate":0.0,"mirror":false},{"o":[0.0,16.0,14.0],"s":[1.0,36.0,20.0],"uv":[74,280],"inflate":0.0,"mirror":false}]}]}};

/* ======================================================================
   Palettes and roster
   ====================================================================== */
const P = (o) => Object.assign({ skin: "#d9a57c", hair: "#4a3324", coat: "#3d4a5c", shirt: "#e8dcc2", pants: "#5b4636",
  boots: "#2e241c", hat: "#2a2522", accent: "#b8862e", metal: "#8f969c", bone: "#e3dccb", glow: "#7fe6ff", eye: "#1d1a17" }, o);

const UNDEAD_KINDS = {
  Cursed: { bone: "#d6cdb4", shirt: "#7a6a52", coat: "#4a4036", glow: "#7fe6ff" },
  Bone: { bone: "#f1ebdc", shirt: "#8a8170", coat: "#5d5548", glow: "#bfe3ff" },
  Barnacle: { bone: "#8fb2a4", shirt: "#3f5e55", coat: "#2f4740", glow: "#9dffd8" },
  Ember: { bone: "#4a3530", shirt: "#3a2a24", coat: "#2a1f1b", glow: "#ffa040" }
};

// tpl: biped | wild:<name> | ship:<class> | serpent | beast | bigcroc
const ROSTER = [
  // Ships
  ...[["sloop","Sloop"],["schooner","Schooner"],["brigantine","Brigantine"],["brig","Brig"],["frigate","Frigate"],["galleon","Galleon"],["ship_of_the_line","Ship of the Line"]]
    .map(([id, name]) => ({ id: "ship_" + id, name, group: "Ships", tpl: "ship:" + id,
      pal: P({ coat: "#5b3b22", shirt: "#efe4cb", pants: "#3a2616", hat: "#141414", accent: "#b8862e", metal: "#2d2f31" }),
      variants: { Player: {}, Navy: { coat: "#2c3e5c", accent: "#d8c27a", shirt: "#f4f1e8" }, Merchant: { coat: "#6b5236", shirt: "#e6dcc6", accent: "#5f8f53" },
        Pirate: { coat: "#2a2420", shirt: "#3b3631", accent: "#9c2a22" }, Ghost: { coat: "#3e5552", shirt: "#9fc9c0", accent: "#7fe6ff", metal: "#506663" } } })),
  // Undead
  { id: "cursed_grunt", name: "Cursed Grunt", group: "Undead", tpl: "biped", opt: { kind: "skeleton", hat: "bandana" }, pal: P(UNDEAD_KINDS.Cursed), variants: UNDEAD_KINDS },
  { id: "cursed_brute", name: "Cursed Brute", group: "Undead", tpl: "biped", opt: { kind: "skeleton", build: "brute", hat: "none" }, pal: P(UNDEAD_KINDS.Cursed), variants: UNDEAD_KINDS },
  { id: "cursed_captain", name: "Cursed Captain", group: "Undead", tpl: "biped", opt: { kind: "skeleton", hat: "tricorn", coat: true }, pal: P(Object.assign({}, UNDEAD_KINDS.Cursed, { coat: "#6e1f1c", hat: "#221c18" })), variants: UNDEAD_KINDS },
  { id: "thorne", name: "Captain Ashmere Thorne", group: "Undead", tpl: "biped", opt: { kind: "drowned", hat: "tricorn", coat: true, beard: true }, pal: P({ skin: "#7d9e94", hair: "#2a3a37", coat: "#24504c", hat: "#14201e", accent: "#9be0c8", glow: "#9dffd8" }) },
  { id: "dread_captain", name: "The Dread Captain", group: "Undead", tpl: "biped", opt: { kind: "skeleton", build: "brute", hat: "bicorne", coat: true, cape: true, epaulettes: true }, pal: P({ bone: "#cfc4a8", coat: "#1c1414", hat: "#0e0b0b", accent: "#c23a2a", glow: "#ff4a3a" }) },
  // Main characters
  ...[
    ["keeper_wren","Keeper Wren",{ hair: "long", hat: "none", coat: true, female: true },{ skin: "#e3b896", hair: "#c9c3b8", coat: "#c9a43a", shirt: "#e8e0cf" }],
    ["ysolde","Harbormistress Ysolde Vane",{ hat: "tricorn", coat: true, female: true, hair: "long" },{ hair: "#2b1d16", coat: "#24405f", accent: "#d8c27a" }],
    ["tamsin","Tamsin Coil, Bellfounder",{ hat: "bandana", female: true, apron: true },{ skin: "#c9926b", hair: "#7a3d1f", shirt: "#b9a98a", coat: "#5a3a22", hat: "#7b2b20" }],
    ["vey","Commodore Aldous Vey",{ hat: "bicorne", coat: true, beard: true },{ hair: "#9a9a92", coat: "#1f3354", pants: "#e9e6dc", accent: "#d9b85a" }],
    ["galloway","Admiral Hester Galloway",{ hat: "bicorne", coat: true, female: true },{ hair: "#d8d2c4", coat: "#17284a", pants: "#ecebe4", accent: "#e2c36a" }],
    ["harrow","Governor Ottoline Harrow",{ robe: true, female: true, hair: "long" },{ skin: "#efcfb5", hair: "#5e3a24", coat: "#5a2a5e", shirt: "#e8d8ea", accent: "#c9a33b" }],
    ["rook","Captain Rook Varga",{ hat: "tricorn", coat: true, beard: true },{ skin: "#b9805a", hair: "#151210", coat: "#1b1a1a", shirt: "#7d1e1e", accent: "#a52a24" }],
    ["thwaite","Professor Ansel Thwaite",{ coat: true, beard: true, hat: "none" },{ hair: "#b7b0a4", coat: "#7a6243", shirt: "#ece3cf", pants: "#5d4e3d" }],
    ["wayfarer","The Wayfarer",{ hat: "hood", robe: true },{ coat: "#6b6c66", hat: "#55564f", skin: "#c99a73" }],
    ["quill","Odalys Quill, Archivist",{ robe: true, female: true, hair: "long" },{ skin: "#8a5a3e", hair: "#1f1612", coat: "#2f5241", accent: "#c8b46a" }],
    ["isadora","Isadora Vance, Smugglers' Queen",{ hat: "tricorn", coat: true, female: true, hair: "long" },{ hair: "#4a1d14", coat: "#7a1b1f", accent: "#d4a53a" }],
    ["celestine","Mother Celestine",{ hat: "hood", robe: true, female: true },{ coat: "#ece8de", hat: "#22222a", accent: "#b39a4a" }],
    ["sorrel","Sorrel Vane-Ashby, Medium",{ hat: "hood", robe: true, female: true },{ coat: "#4b2a5c", hat: "#3a1f48", accent: "#c6a2e8", glow: "#c6a2e8" }],
    ["ibbie","Ibbie Crane, Naturalist",{ hat: "cap", female: true, coat: true },{ coat: "#9b8a5e", hat: "#cdbd8f", shirt: "#efe7d4", pants: "#6e5d3f" }],
    ["dagny","Foreman Dagny Bell",{ hat: "cap", female: true, apron: true },{ hat: "#5f5446", shirt: "#8a6c4c", pants: "#3d3a36", accent: "#e0b84a" }],
    ["old_mariner","The Old Mariner",{ hat: "bandana", beard: true },{ skin: "#c48b62", hair: "#e8e4dc", hat: "#2c4a7a", shirt: "#ddd2bb", pants: "#45505c" }]
  ].map(([id, name, opt, pal]) => ({ id, name, group: "Main characters", tpl: "biped", opt, pal: P(pal) })),
  // Town folk
  ...[
    ["mags","Mags Tallow, Brethren Broker",{ hat: "bandana", female: true },{ hat: "#8c2a22", shirt: "#c9b28a", coat: "#4a3426" }],
    ["jory","Lucky Jory Bones, Dice Man",{ hat: "none", coat: true },{ coat: "#3f6b3a", shirt: "#ece3d0", hair: "#704424" }],
    ["dealer","Card Dealer",{ hat: "none", coat: true },{ coat: "#151515", shirt: "#f0ece4", accent: "#a52a24" }],
    ["fairweather","Cornelius Fairweather, Banker",{ hat: "cap", coat: true, beard: true },{ coat: "#22201e", hat: "#1a1817", hair: "#8d8579", accent: "#c7a64a" }],
    ["pip","Pip Halyard, Regatta Master",{ hat: "cap" },{ shirt: "#e9e4d8", coat: "#2b5b8a", hat: "#efefe8", accent: "#2b5b8a" }],
    ["lott","Barnaby Lott, Auctioneer",{ hat: "none", coat: true },{ coat: "#6b4a2b", shirt: "#e8dcc2", hair: "#3a2a1c" }],
    ["finch","Marlowe Finch, Contest Judge",{ hat: "cap", coat: true },{ coat: "#3a5a7a", hat: "#3a3632" }],
    ["grista","Grista Vell, Arena Master",{ hat: "none", female: true, hair: "long", build: "brute" },{ shirt: "#5a3c26", coat: "#3a2c22", hair: "#9b2f1d", metal: "#9aa0a6" }],
    ["ned_flint","Corporal Ned Flint, Range Master",{ hat: "tricorn", coat: true },{ coat: "#7a1f1f", hat: "#151515", pants: "#ecebe4" }],
    ["bess","Bess Tankard, Tavern Keeper",{ robe: true, female: true, apron: true, hair: "long" },{ coat: "#8a3b2a", shirt: "#efe6d0", hair: "#b35c2a" }],
    ["skip","Skip Lanyard, Course Master",{ hat: "bandana" },{ shirt: "#e9e2d2", hat: "#2c5a8a", pants: "#3a4652" }],
    ["deedes","Percival Deedes, Land Agent",{ hat: "none", coat: true },{ coat: "#6a6a64", shirt: "#ecebe4", hair: "#b8a888" }],
    ["marsh","Clerk Abel Marsh, Bounty Office",{ hat: "cap", coat: true },{ coat: "#26241f", hat: "#26241f", shirt: "#e9e5da" }],
    ["sounding","Tobiah Sounding, Salvage Diver",{ hat: "cap", beard: true },{ shirt: "#3e4f56", pants: "#2c3a40", hat: "#b88a3a", metal: "#b88a3a" }],
    ["kettle","Sergeant Maud Kettle, Harbour Guns",{ hat: "tricorn", coat: true, female: true },{ coat: "#25395e", hat: "#121212", accent: "#d9b85a" }],
    ["flossie","Flossie Snips, Barber",{ robe: true, female: true, hair: "long", apron: true },{ coat: "#c97a8a", shirt: "#f5e9ec", hair: "#e0c060" }],
    ["moss","Ink-Eyed Moss, Tattooist",{ hat: "none", hair: "bald", tattoo: true },{ skin: "#b88762", shirt: "#3b3a36", pants: "#2a2826", accent: "#2b4a7a" }]
  ].map(([id, name, opt, pal]) => ({ id, name, group: "Town folk", tpl: "biped", opt, pal: P(pal) })),
  // Generic people
  ...[
    ["quartermaster","Quartermaster",{ hat: "bandana", apron: true, beard: true },{ shirt: "#c9b28a", hat: "#3d5a3a" }],
    ["shipwright","Shipwright",{ hat: "cap", apron: true },{ shirt: "#d8c9a8", hat: "#6b5a44", coat: "#6a4a2a" }],
    ["marine","Tidewatch Marine",{ hat: "tricorn", coat: true },{ coat: "#23406a", hat: "#111", pants: "#efede6", accent: "#e4e0d2" }],
    ["crew_gunner","Crew: Gunner",{ hat: "bandana" },{ hat: "#7a1f1f", shirt: "#cfc4ad" }],
    ["crew_bosun","Crew: Bosun",{ hat: "cap", beard: true, build: "brute" },{ hat: "#2c3e50", shirt: "#e6e0d2" }],
    ["crew_navigator","Crew: Navigator",{ hat: "tricorn", coat: true },{ coat: "#4a6a5a", hat: "#202020" }],
    ["bounty_hunter","Bounty Hunter",{ hat: "tricorn", coat: true, beard: true },{ coat: "#3a3028", hat: "#1a1714", accent: "#8c7a55" }]
  ].map(([id, name, opt, pal]) => ({ id, name, group: "Generic people", tpl: "biped", opt, pal: P(pal) })),
  // Bosses
  { id: "tide_sovereign", name: "The Tide Sovereign", group: "Bosses", tpl: "biped", opt: { kind: "drowned", hat: "crown", robe: true, build: "brute", spikes: true, cape: true, staff: "trident" }, pal: P({ skin: "#5d8f9a", coat: "#1d4d5c", accent: "#9ee8ff", hat: "#c7b06a", glow: "#9ee8ff" }) },
  { id: "lampwrights_hand", name: "The Lampwright's Hand", group: "Bosses", tpl: "biped", opt: { hat: "hood", robe: true, lantern: true, cape: true }, pal: P({ coat: "#1c1a20", hat: "#141218", accent: "#f2c45a", glow: "#f2c45a" }) },
  { id: "cinder_king", name: "The Cinder King", group: "Bosses", tpl: "biped", opt: { kind: "skeleton", build: "brute", hat: "crown", robe: true, spikes: true, cape: true }, pal: P(Object.assign({}, UNDEAD_KINDS.Ember, { coat: "#3a1a10", hat: "#d4a53a" })) },
  { id: "veil_witch", name: "The Veil Witch", group: "Bosses", tpl: "biped", opt: { hat: "hood", robe: true, female: true, veil: true, staff: "crook" }, pal: P({ skin: "#b9c7bf", coat: "#3a4a4e", hat: "#2a383b", glow: "#c6f2e8" }) },
  { id: "red_maddox", name: "Red Maddox, Pirate Warlord", group: "Bosses", tpl: "biped", opt: { hat: "tricorn", coat: true, beard: true, build: "brute", epaulettes: true, eyepatch: true, cape: true }, pal: P({ hair: "#a3301e", coat: "#6e1612", hat: "#1b1210", accent: "#d4a53a" }) },
  { id: "ashen_herald", name: "The Ashen Herald", group: "Bosses", tpl: "biped", opt: { kind: "skeleton", hat: "hood", robe: true, censer: true, staff: "crook" }, pal: P(Object.assign({}, UNDEAD_KINDS.Ember, { coat: "#4a4442", hat: "#3a3432" })) },
  { id: "pretender", name: "The Pretender", group: "Bosses", tpl: "biped", opt: { kind: "drowned", hat: "crown", coat: true, cape: true, epaulettes: true }, pal: P({ skin: "#9cb3ad", coat: "#2a3a5a", hat: "#9aa5b0", accent: "#c7d2dc", glow: "#c7d2dc" }) },
  { id: "abyssal", name: "The Abyssal", group: "Bosses", tpl: "serpent", pal: P({ skin: "#26343f", shirt: "#3e5a63", accent: "#7a2d3d", glow: "#7fe6ff", bone: "#d9d2bd" }) },
  { id: "brannoch", name: "Brannoch, Who Drowned the Fire", group: "Bosses", tpl: "biped", opt: { kind: "drowned", beard: true, hat: "none", build: "brute", seaweed: true }, pal: P({ skin: "#6b8f86", hair: "#2a3d38", shirt: "#3a4f4a", glow: "#9dffd8" }) },
  { id: "grey_lamplighter", name: "The Grey Lamplighter", group: "Bosses", tpl: "biped", opt: { kind: "skeleton", hat: "hood", robe: true, lantern: true, staff: "pole" }, pal: P({ bone: "#cfcabd", coat: "#6a6864", hat: "#5a5854", glow: "#ffd27a" }) },
  { id: "mother_of_gulls", name: "Mother of Gulls", group: "Bosses", tpl: "biped", opt: { hat: "hood", robe: true, female: true, wings: true }, pal: P({ coat: "#e6e2d8", hat: "#cfcabc", skin: "#c9a78a", accent: "#4a4a48" }) },
  { id: "bone_abbot", name: "The Bone Abbot", group: "Bosses", tpl: "biped", opt: { kind: "skeleton", hat: "mitre", robe: true, build: "brute", staff: "crook", cape: true }, pal: P({ bone: "#ece4cf", coat: "#2a2430", hat: "#e2d6b0", accent: "#b39a4a", glow: "#9fd0ff" }) },
  { id: "cinder_matron", name: "The Cinder Matron", group: "Bosses", tpl: "biped", opt: { kind: "drowned", robe: true, female: true, hat: "hood", spikes: true, censer: true }, pal: P({ skin: "#5a3328", coat: "#3a1c14", hat: "#2a140e", glow: "#ff8a3a" }) },
  { id: "old_gnasher", name: "Old Gnasher", group: "Bosses", tpl: "bigcroc", pal: P({ skin: "#4d5f3a", shirt: "#c8c49a", accent: "#2f3b24", bone: "#efe8d2" }) },
  { id: "grubb", name: "Claim-Jumper Grubb", group: "Bosses", tpl: "biped", opt: { hat: "cap", lamp: true, beard: true, build: "brute", apron: true }, pal: P({ hat: "#6a5a44", shirt: "#7a5a3a", pants: "#3d3a36", hair: "#5a3a22", metal: "#a8a29a" }) },
  { id: "mother_brine", name: "Mother Brine", group: "Bosses", tpl: "biped", opt: { kind: "drowned", robe: true, female: true, hair: "long", seaweed: true, staff: "trident" }, pal: P({ skin: "#6f9a92", hair: "#2c4f48", coat: "#2b4a46", glow: "#9dffd8" }) },
  { id: "slag_beast", name: "The Slag Beast", group: "Bosses", tpl: "beast", pal: P({ skin: "#3a3634", shirt: "#55504c", glow: "#ff7a2a", bone: "#8a827a" }) },
  { id: "lantern_warden", name: "The Lantern Warden", group: "Bosses", tpl: "biped", opt: { hat: "hood", robe: true, build: "brute", chestlight: true, lantern: true }, pal: P({ coat: "#e8e2cf", hat: "#d8d0b8", skin: "#c9b49a", glow: "#fff1b0", accent: "#c9a33b" }) },
  { id: "corvin_hale", name: "Dread Admiral Corvin Hale", group: "Bosses", tpl: "biped", opt: { kind: "skeleton", build: "brute", hat: "bicorne", coat: true, cape: true, epaulettes: true, staff: "pole" }, pal: P({ bone: "#d8ccb0", coat: "#14161e", hat: "#0b0c10", accent: "#3a6ad0", glow: "#7fb0ff" }) },
  { id: "captain_saltbones", name: "Captain Saltbones", group: "Bosses", tpl: "biped", opt: { kind: "skeleton", hat: "tricorn", coat: true, cape: true, eyepatch: true }, pal: P({ bone: "#e2dccb", coat: "#3b4a3a", hat: "#22281f", accent: "#d8c27a", glow: "#a8f0d0" }) },
  { id: "barnacle_bride", name: "The Barnacle Bride", group: "Bosses", tpl: "biped", opt: { kind: "drowned", female: true, robe: true, veil: true, hat: "hood", seaweed: true }, pal: P({ skin: "#7f9a8c", coat: "#d8d4c6", hat: "#e8e4d6", accent: "#6a8a5a", glow: "#bfffe8" }) },
  { id: "hangman", name: "The Hangman of Bloodwake", group: "Bosses", tpl: "biped", opt: { hat: "hood", build: "brute", apron: true, staff: "crook" }, pal: P({ skin: "#b08460", coat: "#4a2a20", shirt: "#5a3a2a", hat: "#1a1210", accent: "#8a6a3a", pants: "#3a2a20" }) },
  { id: "witchlight_hag", name: "The Witchlight Hag", group: "Bosses", tpl: "biped", opt: { hat: "hood", robe: true, female: true, lantern: true, staff: "crook" }, pal: P({ skin: "#8aa070", coat: "#3a4a2a", hat: "#2a3420", accent: "#9acd5a", glow: "#c8ff6a" }) },
  { id: "signal_keeper", name: "The Signal Keeper", group: "Bosses", tpl: "biped", opt: { kind: "skeleton", hat: "bicorne", coat: true, epaulettes: true, lantern: true }, pal: P({ bone: "#d6cfbd", coat: "#2a3a6a", hat: "#1a2240", accent: "#e0c060", glow: "#ffd27a" }) },
  { id: "reach_serpent", name: "Reach Serpent", group: "Sea monsters", tpl: "serpent", pal: P({ skin: "#2f5a46", shirt: "#8ab88a", accent: "#c8503a", glow: "#f2e66a", bone: "#e0dcc8" }) },
  { id: "siren", name: "Siren of the Shoals", group: "Sea monsters", tpl: "biped", opt: { kind: "drowned", female: true, hair: "long", seaweed: true, robe: true }, pal: P({ skin: "#8fc0b8", hair: "#1f6a72", coat: "#2a5a66", accent: "#6ad0c8", glow: "#c8fff4" }) },
  // Wildlife
  ...[["crocodile","Crocodile",{ skin: "#4f6a3c", shirt: "#c9c499" }],["shark","Shark",{ skin: "#6a7f8c", shirt: "#e9ecea" }],["jaguar","Jaguar",{ skin: "#d39a3e", shirt: "#f1e2c2", accent: "#2a1e16" }],
      ["boar","Boar",{ skin: "#5d4433", shirt: "#3a2a1f", bone: "#efe6d0" }],["crab","Crab",{ skin: "#b9452e", shirt: "#e6b8a0" }],["snake","Snake",{ skin: "#4d7a34", shirt: "#d4cf82", accent: "#2a3f1d" }],
      ["monkey","Monkey",{ skin: "#6a4a32", shirt: "#d9b894" }],["tentacle","Leviathan Tentacle",{ skin: "#4a2e52", shirt: "#d9a7c9" }]]
    .map(([id, name, pal]) => ({ id: "wild_" + id, name, group: "Wildlife", tpl: "wild:" + id, pal: P(pal) }))
];

/* ======================================================================
   Small helpers
   ====================================================================== */
const $ = (s, r = document) => r.querySelector(s);
const el = (tag, attrs = {}, ...kids) => {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === "class") e.className = v; else if (k === "text") e.textContent = v;
    else if (k.startsWith("on")) e.addEventListener(k.slice(2), v); else if (v !== false && v != null) e.setAttribute(k, v);
  }
  for (const k of kids) if (k != null) e.append(k);
  return e;
};
const clone = (o) => JSON.parse(JSON.stringify(o));
const hex2rgb = (h) => { h = h.replace("#", ""); if (h.length === 3) h = h.split("").map(c => c + c).join(""); const n = parseInt(h, 16); return [(n >> 16) & 255, (n >> 8) & 255, n & 255]; };
const shade = (rgb, f) => rgb.map(c => Math.max(0, Math.min(255, Math.round(f >= 0 ? c + (255 - c) * f : c * (1 + f)))));
function hash(a, b, c) { let h = (a * 374761393 + b * 668265263 + c * 2147483647) | 0; h = (h ^ (h >>> 13)) * 1274126177 | 0; return ((h ^ (h >>> 16)) >>> 0) / 4294967296; }
let toastTimer;
function toast(msg) { const t = $("#toast"); t.textContent = msg; t.hidden = false; clearTimeout(toastTimer); toastTimer = setTimeout(() => t.hidden = true, 2600); }

/* ======================================================================
   Model generators. Coordinates are Minecraft model pixels: y grows downward,
   the feet stand at y 24, the front faces -z (ships: bow at +z).
   Part names follow the mod's animation rules.
   ====================================================================== */
const part = (name, parent, pivot, rot, cubes) => ({ name, parent: parent || null, pivot: pivot || [0, 0, 0], rot: rot || [0, 0, 0], cubes });
const cube = (o, s, pat, col, extra) => Object.assign({ o, s, uv: [0, 0], inflate: 0, mirror: false, pat, col }, extra || {});

function biped(opt) {
  const sk = opt.kind === "skeleton", dr = opt.kind === "drowned", brute = opt.build === "brute";
  const flesh = sk ? "bone" : "skin";
  const limb = sk ? 2 : brute ? 5 : opt.female ? 3 : 4;
  const parts = [];
  const head = [cube([-4, -8, -4], [8, 8, 8], sk ? "skull" : "head", flesh)];
  if (opt.beard && !sk) head.push(cube([-4, -2, -4.6], [8, 3, 1], "beard", "hair"));
  parts.push(part("head", null, [0, 0, 0], [0, 0, 0], head));
  const hat = [];
  if (opt.hat === "tricorn") { hat.push(cube([-6, -9, -6], [12, 1, 12], "hat", "hat"), cube([-4.5, -11.5, -4.5], [9, 3, 9], "hat", "hat"), cube([-5.5, -10.5, -6.5], [11, 2, 1], "trim", "accent")); }
  else if (opt.hat === "bicorne") { hat.push(cube([-7, -12, -2], [14, 4, 4], "hat", "hat"), cube([-4.5, -9, -4.5], [9, 1, 9], "hat", "hat"), cube([-1, -13, -2.5], [2, 2, 1], "trim", "accent")); }
  else if (opt.hat === "cap") { hat.push(cube([-4.5, -9.5, -4.5], [9, 2, 9], "hat", "hat"), cube([-4.5, -8, -6.5], [9, 1, 3], "hat", "hat")); if (opt.lamp) hat.push(cube([-1, -10, -5.2], [2, 2, 1], "glow", "glow")); }
  else if (opt.hat === "bandana") { hat.push(cube([-4.5, -8.6, -4.5], [9, 3, 9], "cloth", "hat"), cube([-1, -6.5, 4.4], [2, 3, 1], "cloth", "hat")); }
  else if (opt.hat === "hood") { hat.push(cube([-4.6, -8.6, -4.6], [9.2, 9.2, 9.2], "hood", "hat")); }
  else if (opt.hat === "crown") { hat.push(cube([-4.5, -10, -4.5], [9, 2, 9], "metal", "hat"), ...[[-4.5, -4.5], [3.5, -4.5], [-4.5, 3.5], [3.5, 3.5], [-0.5, -4.5]].map(([x, z]) => cube([x, -12, z], [1, 2, 1], "glow", "glow"))); }
  else if (opt.hat === "mitre") { hat.push(cube([-3.5, -14, -3], [7, 6, 6], "hat", "hat"), cube([-0.5, -15, -3.2], [1, 6, 1], "trim", "accent")); }
  if (opt.hair === "long" && !sk && opt.hat !== "hood") hat.push(cube([-4.3, -8.3, 0.5], [8.6, 10, 4], "hair", "hair"));
  if (opt.veil) hat.push(cube([-4.4, -6, -4.9], [8.8, 7, 0.4], "veil", "hat"));
  if (opt.eyepatch) head.push(cube([1, -5, -4.3], [2, 2, 0.3], "plain", "hat"));
  if (hat.length) parts.push(part("hat", null, [0, 0, 0], [0, 0, 0], hat));
  const body = [cube([-4, 0, -2], [8, 12, 4], sk ? "ribs" : "torso", sk ? "bone" : "shirt")];
  if (brute) body[0] = cube([-5, 0, -3], [10, 12, 6], sk ? "ribs" : "torso", sk ? "bone" : "shirt");
  const bw = brute ? 5 : 4, bd = brute ? 3 : 2;
  if (opt.coat) body.push(cube([-bw - 0.5, 0, -bd - 0.5], [bw * 2 + 1, 12, bd * 2 + 1], "coat", "coat"), cube([-bw - 0.5, 12, bd - 0.5], [bw * 2 + 1, 6, 1.5], "coat", "coat"));
  if (opt.robe) body.push(cube([-bw - 0.6, 0, -bd - 0.6], [bw * 2 + 1.2, 12, bd * 2 + 1.2], "coat", "coat"), cube([-bw - 0.8, 12, -bd - 0.8], [bw * 2 + 1.6, 11, bd * 2 + 1.6], "robe", "coat"));
  if (opt.apron) body.push(cube([-bw + 0.5, 3, -bd - 0.6], [bw * 2 - 1, 13, 0.6], "cloth", "accent"));
  if (dr) body.push(cube([-bw, 9, -bd - 0.4], [bw * 2, 1, 0.4], "trim", "glow"));
  // Boss dressings
  if (opt.cape) body.push(cube([-bw - 0.5, 0, bd + 0.6], [bw * 2 + 1, 20, 1], "robe", "accent"));
  if (opt.wings) for (const sd of [-1, 1]) body.push(cube(sd < 0 ? [-bw - 9, -2, bd] : [bw, -2, bd], [9, 14, 1], "feathers", "coat"));
  if (opt.epaulettes) for (const sd of [-1, 1]) body.push(cube(sd < 0 ? [-bw - 2, -1, -bd - 0.5] : [bw - 1, -1, -bd - 0.5], [3, 1.5, bd * 2 + 1], "trim", "accent"));
  if (opt.spikes) for (const sd of [-1, 1]) for (let k = 0; k < 3; k++) body.push(cube([sd < 0 ? -bw - 1 + k : bw - k, -3 - k * 1.5, -1 + (k % 2)], [1, 2 + k, 1], "glow", "glow"));
  if (opt.seaweed) for (let k = 0; k < 5; k++) body.push(cube([-bw + k * (bw * 2 / 5), 2 + (k % 2) * 2, -bd - 0.7], [1, 9 + (k % 3) * 2, 0.5], "cloth", "accent"));
  if (opt.chestlight) body.push(cube([-1.5, 3, -bd - 2.5], [3, 4, 2], "glow", "glow"));
  if (opt.censer) body.push(cube([bw - 1, 10, -bd - 2], [2, 2, 2], "glow", "glow"));
  parts.push(part("body", null, [0, 0, 0], [0, 0, 0], body));
  const ax = brute ? 6 : 5, lx = brute ? 2.4 : 1.9, armPat = sk ? "bonelimb" : "arm", legPat = sk ? "bonelimb" : "leg";
  const half = limb / 2;
  const arm = (side) => {
    const c = [cube(side < 0 ? [-limb + 1, -2, -half] : [-1, -2, -half], [limb, 12, limb], armPat, sk ? "bone" : (opt.coat || opt.robe ? "coat" : "shirt"))];
    if ((opt.coat || opt.robe) && sk) c.push(cube(side < 0 ? [-limb + 0.5, -2.5, -half - 0.5] : [-1.5, -2.5, -half - 0.5], [limb + 1, 7, limb + 1], "coat", "coat"));
    return c;
  };
  const ra = arm(-1), la = arm(1);
  // A staff or crook held upright in the right hand, a lantern hanging from the left
  if (opt.staff) ra.push(cube([-limb / 2 - 0.5, -8, -half - 3], [1, 26, 1], "wood", "pants"), cube([-limb / 2 - 1, -11, -half - 3.5], [2, 3, 2], opt.staff === "crook" ? "trim" : "glow", opt.staff === "crook" ? "accent" : "glow"));
  if (opt.lantern) la.push(cube([-0.5, 10, -half - 1], [1, 2, 1], "metal", "metal"), cube([-1.5, 12, -half - 2], [3, 4, 3], "glow", "glow"));
  parts.push(part("right_arm", null, [-ax, 2, 0], [0, 0, 0], ra));
  parts.push(part("left_arm", null, [ax, 2, 0], [0, 0, 0], la));
  const leg = () => [cube([-half, 0, -half], [limb, 12, limb], legPat, sk ? "bone" : "pants")];
  parts.push(part("right_leg", null, [-lx, 12, 0], [0, 0, 0], leg()));
  parts.push(part("left_leg", null, [lx, 12, 0], [0, 0, 0], leg()));
  return { tex: [64, 64], parts, kind: opt.kind || "human", opt };
}

function shipModel(cls) {
  const C = { sloop: [52, 20, 13, 1, 2, 0], schooner: [60, 20, 13, 2, 2, 0], brigantine: [66, 24, 15, 2, 3, 0], brig: [70, 24, 15, 2, 4, 0],
    frigate: [80, 26, 17, 3, 5, 0], galleon: [84, 30, 20, 3, 4, 1], ship_of_the_line: [96, 32, 24, 3, 7, 1] }[cls];
  const [L, B, H, masts, guns, castle] = C;
  const p = [], h = L / 2, b = B / 2, top = -H + 3;
  p.push(part("keel", null, [0, 0, 0], [0, 0, 0], [cube([-b, 0, -h], [B, 3, L], "planks", "coat")]));
  p.push(part("side_left", null, [0, 0, 0], [0, 0, 0], [cube([-b - 2, top, -h], [2, H, L], "hull", "coat")]));
  p.push(part("side_right", null, [0, 0, 0], [0, 0, 0], [cube([b, top, -h], [2, H, L], "hull", "coat")]));
  p.push(part("stern", null, [0, 0, 0], [0, 0, 0], [cube([-b - 2, top, -h - 2], [B + 4, H, 2], "hull", "coat")]));
  p.push(part("bow", null, [0, 0, 0], [0, 0, 0], [cube([-b + 2, top, h], [B - 4, H, 6], "hull", "coat")]));
  p.push(part("bow_tip", null, [0, 0, 0], [0, 0, 0], [cube([-b / 2, top + 1, h + 6], [b, H - 2, 5], "hull", "coat")]));
  p.push(part("deck", null, [0, 0, 0], [0, 0, 0], [cube([-b, -4, -h], [B, 1, L], "deck", "pants")]));
  p.push(part("rail_left", null, [0, 0, 0], [0, 0, 0], [cube([-b - 2, top - 2, -h], [2, 2, L], "wood", "pants")]));
  p.push(part("rail_right", null, [0, 0, 0], [0, 0, 0], [cube([b, top - 2, -h], [2, 2, L], "wood", "pants")]));
  const cabH = castle ? 14 : 8, cabL = castle ? Math.round(L * 0.28) : 10;
  p.push(part("cabin", null, [0, 0, 0], [0, 0, 0], [cube([-b + 1, top - cabH + 1, -h], [B - 2, cabH, cabL], "cabin", "coat"),
    cube([-b, top - cabH, -h - 1], [B, 1, cabL + 2], "deck", "pants")]));
  if (castle) p.push(part("forecastle", null, [0, 0, 0], [0, 0, 0], [cube([-b + 2, top - 6, h - 12], [B - 4, 6, 12], "cabin", "coat")]));
  const mastZ = masts === 1 ? [4] : masts === 2 ? [-Math.round(L * 0.18), Math.round(L * 0.2)] : [-Math.round(L * 0.26), Math.round(L * 0.04), Math.round(L * 0.3)];
  const mastH = Math.round(L * 1.35);
  mastZ.forEach((z, i) => {
    const mh = Math.round(mastH * (i === 1 || masts === 1 ? 1 : 0.85));
    p.push(part("mast" + (i + 1), null, [0, 0, 0], [0, 0, 0], [cube([-1, -mh - 4, z], [2, mh, 2], "wood", "pants")]));
    const yw = Math.round(B * 1.8 * (i === 1 || masts === 1 ? 1 : 0.85));
    p.push(part("yard" + (i + 1), null, [0, 0, 0], [0, 0, 0], [cube([-yw / 2, -mh + 8, z - 1], [yw, 2, 2], "wood", "pants"),
      cube([-yw / 2 + 2, -Math.round(mh * 0.55), z - 1], [yw - 4, 2, 2], "wood", "pants")]));
    const sh = Math.round(mh * 0.55) - 12;
    p.push(part("sail" + (i + 1), null, [0, -mh + 10, 0], [0, 0, 0], [cube([-yw / 2 + 1, 0, z + 2], [yw - 2, sh, 1], "sail", "shirt")]));
  });
  p.push(part("bowsprit", null, [0, 0, 0], [-14, 0, 0], [cube([-1, top - 2, h + 8], [2, 2, Math.round(L * 0.35)], "wood", "pants")]));
  p.push(part("figurehead", null, [0, 0, 0], [0, 0, 0], [cube([-1.5, top + 2, h + 10], [3, 6, 3], "metal", "accent")]));
  const mainMastTop = -Math.round(mastH) - 4;
  p.push(part("flag", null, [0, 0, 0], [0, 0, 0], [cube([0, mainMastTop, mastZ[Math.min(1, mastZ.length - 1)] - 10], [1, 6, 10], "flag", "hat")]));
  p.push(part("emblem", null, [0, -Math.round(mastH * 0.75), mastZ[Math.min(1, mastZ.length - 1)] + 3.6], [0, 90, 0], [cube([0, 0, -5], [1, 6, 10], "flag", "hat")]));
  const decks = castle ? 2 : 1;
  for (let d = 0; d < decks; d++) for (let g = 0; g < guns; g++) {
    const z = Math.round(-h + 14 + g * ((L - 24) / Math.max(1, guns - 1 || 1))) - 2, y = top + 4 + d * 5;
    const sfx = (d ? "b" : "") + (g + 1);
    p.push(part("cannon_l" + sfx, null, [0, 0, 0], [0, 0, 0], [cube([-b - 4, y, z], [2, 2, 4], "metal", "metal")]));
    p.push(part("cannon_r" + sfx, null, [0, 0, 0], [0, 0, 0], [cube([b + 2, y, z], [2, 2, 4], "metal", "metal")]));
  }
  return { tex: [256, 512], parts: p, kind: "ship" };
}

function serpent() {
  const p = [];
  p.push(part("head", null, [0, 8, -10], [0, 0, 0], [cube([-9, -8, -18], [18, 14, 18], "scales", "skin"), cube([-6, -4, -30], [12, 8, 12], "scales", "skin"),
    cube([-9.5, -6, -15], [2, 3, 3], "eyeglow", "glow"), cube([7.5, -6, -15], [2, 3, 3], "eyeglow", "glow")]));
  p.push(part("jaw", "head", [0, 6, -12], [0, 0, 0], [cube([-6, 0, -18], [12, 4, 18], "maw", "accent")]));
  let parent = null, z = -10;
  for (let i = 1; i <= 6; i++) {
    const w = 16 - i * 1.6, hgt = 14 - i * 1.4;
    p.push(part("seg" + i, parent, parent ? [0, 0, 16] : [0, 8, z], [0, 0, 0], [cube([-w / 2, -hgt / 2, 0], [w, hgt, 16], "scales", "skin"), cube([-0.5, -hgt / 2 - 4, 3], [1, 4, 10], "fin", "accent")]));
    parent = "seg" + i;
  }
  return { tex: [256, 256], parts: p, kind: "serpent" };
}

function beast() {
  const p = [];
  p.push(part("body", null, [0, 8, 0], [0, 0, 0], [cube([-8, -7, -12], [16, 13, 24], "rock", "skin"), cube([-6, -11, -8], [12, 4, 14], "rock", "shirt")]));
  p.push(part("head", null, [0, 6, -12], [0, 0, 0], [cube([-5, -5, -10], [10, 10, 10], "rockface", "skin"), cube([-6, -8, -6], [2, 4, 2], "glow", "glow"), cube([4, -8, -6], [2, 4, 2], "glow", "glow")]));
  for (const [n, x, z] of [["leg_fl", -6, -8], ["leg_fr", 6, -8], ["leg_bl", -6, 8], ["leg_br", 6, 8]])
    p.push(part(n, null, [x, 13, z], [0, 0, 0], [cube([-3, 0, -3], [6, 11, 6], "rock", "skin")]));
  p.push(part("tail1", "body", [0, -2, 12], [0, 0, 0], [cube([-2.5, -2, 0], [5, 4, 10], "rock", "skin")]));
  return { tex: [128, 128], parts: p, kind: "beast" };
}

const WILD_PATTERNS = { crocodile: "scales", shark: "skinsmooth", jaguar: "spots", boar: "fur", crab: "shell", snake: "scales", monkey: "fur", tentacle: "suckers" };
function wild(name, scale) {
  const m = clone(MOD_MODELS[name]);
  const s = scale || 1;
  for (const pt of m.parts) {
    pt.pivot = pt.pivot.map((v, i) => i === 1 && !pt.parent ? 24 - (24 - v) * s : v * s);
    for (const c of pt.cubes) {
      c.o = c.o.map(v => v * s); c.s = c.s.map(v => v * s);
      const n = pt.name;
      c.pat = n === "head" || n === "jaw" && false ? "animalhead" : (WILD_PATTERNS[name] || "fur");
      c.col = n.startsWith("claw") || n === "jaw" ? "shirt" : "skin";
    }
  }
  m.kind = "wild";
  return m;
}
function shipTemplateFromMod() { const m = clone(MOD_MODELS.ship); for (const pt of m.parts) for (const c of pt.cubes) { c.pat = pt.name.startsWith("sail") ? "sail" : pt.name.startsWith("cannon") ? "metal" : "hull"; c.col = pt.name.startsWith("sail") ? "shirt" : "coat"; } return m; }

function buildTemplate(entry) {
  let m;
  if (entry.tpl === "biped") m = biped(entry.opt || {});
  else if (entry.tpl.startsWith("ship:")) m = shipModel(entry.tpl.slice(5));
  else if (entry.tpl.startsWith("wild:")) m = wild(entry.tpl.slice(5));
  else if (entry.tpl === "serpent") m = serpent();
  else if (entry.tpl === "beast") m = beast();
  else if (entry.tpl === "bigcroc") { m = wild("crocodile", 1.7); m.tex = [128, 256]; }
  return { id: entry.id, name: entry.name, model: m, palette: clone(entry.pal), status: "started", notes: "", texture: null, version: 1 };
}

/* ======================================================================
   UV layout: Minecraft box UV, packed onto the smallest texture that fits
   ====================================================================== */
function boxSize(c) { const w = Math.ceil(c.s[0]), h = Math.ceil(c.s[1]), d = Math.ceil(c.s[2]); return [Math.max(1, 2 * (w + d)), Math.max(1, d + h)]; }
function autoUV(model) {
  const all = [];
  for (const p of model.parts) for (const c of p.cubes) all.push(c);
  const order = all.slice().sort((a, b) => boxSize(b)[1] - boxSize(a)[1] || boxSize(b)[0] - boxSize(a)[0]);
  const widest = Math.max(16, ...all.map(c => boxSize(c)[0]));
  let W = 32; while (W < widest) W *= 2;
  for (let attempt = 0; attempt < 8; attempt++) {
    let x = 0, y = 0, rowH = 0;
    for (const c of order) {
      const [bw, bh] = boxSize(c);
      if (x + bw > W) { x = 0; y += rowH; rowH = 0; }
      c.uv = [x, y]; x += bw; rowH = Math.max(rowH, bh);
    }
    const used = y + rowH;
    let H = 32; while (H < used) H *= 2;
    if (H <= W * 2) { model.tex = [W, H]; return; }
    W *= 2;
  }
}

/* ======================================================================
   Painting: lay down a painted base from the palette, face by face
   ====================================================================== */
function faceRects(c) {
  const [u, v] = c.uv, w = Math.ceil(c.s[0]), h = Math.ceil(c.s[1]), d = Math.ceil(c.s[2]);
  return { top: [u + d, v, w, d], bottom: [u + d + w, v, w, d], west: [u, v + d, d, h], north: [u + d, v + d, w, h], east: [u + d + w, v + d, d, h], south: [u + d + w + d, v + d, w, h] };
}
function paintModel(proj, img) {
  const [W, H] = proj.model.tex, pal = proj.palette, data = img.data;
  data.fill(0);
  const put = (x, y, rgb, a = 255) => { if (x < 0 || y < 0 || x >= W || y >= H) return; const i = (y * W + x) * 4; data[i] = rgb[0]; data[i + 1] = rgb[1]; data[i + 2] = rgb[2]; data[i + 3] = a; };
  let seed = 1;
  for (const p of proj.model.parts) for (const c of p.cubes) {
    seed++;
    const base = hex2rgb(pal[c.col] || c.col || "#888888");
    const pat = c.pat || "plain";
    const R = faceRects(c);
    for (const [face, [fx, fy, fw, fh]] of Object.entries(R)) {
      const light = face === "top" ? 0.12 : face === "bottom" ? -0.25 : face === "north" ? 0.02 : face === "south" ? -0.06 : -0.1;
      for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
        const n = hash(x + fx, y + fy, seed) - 0.5;
        let col = shade(base, light + n * 0.09);
        const gy = y / Math.max(1, fh - 1);
        let hole = false;
        switch (pat) {
          case "planks": case "hull": case "deck": if (y % 3 === 2) col = shade(col, -0.22); if ((x + Math.floor(y / 3) * 7) % 13 === 0) col = shade(col, -0.15); if (pat === "hull" && face !== "top" && face !== "bottom" && y === 1) col = shade(hex2rgb(pal.accent), -0.1); break;
          case "wood": if ((x + y) % 4 === 0) col = shade(col, -0.12); break;
          case "sail": if (x % 6 === 0) col = shade(col, -0.08); col = shade(col, -0.08 * gy); break;
          case "cabin": if (face !== "top" && face !== "bottom" && y > 2 && y < fh - 2 && x % 5 === 2) col = hex2rgb("#f3d58a"); break;
          case "flag": col = shade(base, n * 0.04); break;
          case "metal": col = shade(base, light + n * 0.05 + (y === 0 ? 0.2 : 0)); break;
          case "torso": if (y === Math.round(fh * 0.72) && face !== "top" && face !== "bottom") col = shade(hex2rgb(pal.boots), 0.05); if (face === "north" && y === Math.round(fh * 0.72) && Math.abs(x - fw / 2) < 1) col = hex2rgb(pal.accent); break;
          case "coat": if (face === "north" && Math.abs(x - (fw - 1) / 2) < 0.6) col = shade(col, -0.3); if (face === "north" && Math.abs(x - (fw - 1) / 2) < 1.6 && y % 3 === 1 && y < fh - 1) col = hex2rgb(pal.accent); if (y === fh - 1 && face !== "top") col = shade(col, -0.2); break;
          case "robe": col = shade(col, -0.1 * gy); if (y === fh - 1) col = shade(hex2rgb(pal.accent), -0.1); break;
          case "arm": if (face !== "top" && y >= fh - 3) col = shade(hex2rgb(pal.skin), light + n * 0.06); if (face === "bottom") col = shade(hex2rgb(pal.skin), -0.2); break;
          case "leg": if (face !== "top" && y >= fh - 4) col = shade(hex2rgb(pal.boots), light + n * 0.05); if (face === "bottom") col = hex2rgb(pal.boots); break;
          case "bonelimb": if (y % 6 === 5) col = shade(col, -0.25); break;
          case "ribs": if (face === "north" || face === "south") { if (y > 1 && y < fh - 3 && y % 2 === 0 && x > 0 && x < fw - 1) col = shade(col, -0.45); if (Math.abs(x - (fw - 1) / 2) < 0.6) col = shade(base, 0.05); } break;
          case "hat": if (y === fh - 1 && face !== "top" && face !== "bottom") col = shade(col, -0.25); break;
          case "trim": col = shade(base, 0.1 + n * 0.05); break;
          case "cloth": if ((x + y) % 4 === 0) col = shade(col, -0.12); break;
          case "hood": if (face === "north" && x > 0 && x < fw - 1 && y > 1) { col = [18, 15, 14]; if (y > 2) hole = true; } break;
          case "hair": case "beard": col = shade(base, n * 0.16 + (x % 2 ? -0.06 : 0)); break;
          case "scales": if ((x + (y % 2) * 2) % 4 === 0 || y % 3 === 0) col = shade(col, -0.16); if (face === "bottom") col = shade(hex2rgb(pal.shirt), n * 0.08); break;
          case "skinsmooth": if (face === "bottom" || (face !== "top" && gy > 0.62)) col = shade(hex2rgb(pal.shirt), n * 0.05); break;
          case "spots": if (hash(Math.floor((x + fx) / 2), Math.floor((y + fy) / 2), 7) > 0.78) col = hex2rgb(pal.accent); if (face === "bottom") col = hex2rgb(pal.shirt); break;
          case "fur": col = shade(col, (hash(x + fx, y + fy, 3) - 0.5) * 0.22); break;
          case "shell": if (face === "top" && (x + y) % 5 === 0) col = shade(col, 0.18); if (face === "bottom") col = hex2rgb(pal.shirt); break;
          case "suckers": if (face === "bottom" && y % 3 === 1 && x % 2 === 0) col = hex2rgb(pal.shirt); break;
          case "rock": case "rockface": if (hash(x + fx, y + fy, 11) > 0.93) col = hex2rgb(pal.glow); break;
          case "maw": if (face === "top" && (x % 3 === 0)) col = hex2rgb(pal.bone); break;
          case "fin": col = shade(col, -0.05 * gy); break;
          case "glow": case "eyeglow": col = shade(hex2rgb(pal.glow), n * 0.1); break;
          case "feathers": if ((x + y * 2) % 5 === 0) col = shade(col, -0.2); if (y % 4 === 3) col = shade(col, 0.1); break;
          case "veil": if ((face === "north" || face === "south") && y > 0 && (x + y) % 2 === 0) hole = true; else col = shade(col, -0.15); break;
        }
        put(fx + x, fy + y, col, hole ? 0 : 255);
      }
    }
    // Faces: eyes, brows, mouth on the front of heads
    if (pat === "head" || pat === "skull" || pat === "animalhead" || pat === "rockface") {
      const [fx, fy, fw, fh] = R.north;
      const ey = fy + Math.round(fh * 0.5), lx = fx + Math.max(1, Math.round(fw / 8)), rx = fx + fw - 1 - Math.max(1, Math.round(fw / 8));
      if (pat === "skull") {
        const glow = hex2rgb(pal.glow);
        for (const ex of [lx, rx]) { put(ex, ey, glow); put(ex - (ex === lx ? 1 : -1), ey, [20, 16, 14]); put(ex, ey - 1, [20, 16, 14]); }
        put(fx + Math.round(fw / 2), ey + 1, [30, 24, 20]);
        for (let x = fx + 2; x < fx + fw - 2; x++) put(x, fy + fh - 2, x % 2 ? [30, 24, 20] : shade(hex2rgb(pal.bone), 0.1));
      } else if (pat === "head") {
        const white = [240, 236, 228], eye = hex2rgb(pal.eye), hair = hex2rgb(pal.hair);
        put(lx, ey, white); put(lx + 1, ey, eye); put(rx - 1, ey, eye); put(rx, ey, white);
        for (let x = lx; x <= lx + 1; x++) put(x, ey - 1, shade(hair, -0.1));
        for (let x = rx - 1; x <= rx; x++) put(x, ey - 1, shade(hair, -0.1));
        put(fx + Math.round(fw / 2) - 1, ey + 2, shade(hex2rgb(pal.skin), -0.3)); put(fx + Math.round(fw / 2), ey + 2, shade(hex2rgb(pal.skin), -0.3));
        // hair on top and back, a fringe at the front
        const T = R.top, S = R.south;
        if (proj.model.opt?.hair !== "bald") {
          for (let y = 0; y < T[3]; y++) for (let x = 0; x < T[2]; x++) put(T[0] + x, T[1] + y, shade(hair, (hash(x, y, 5) - 0.5) * 0.15));
          for (let y = 0; y < Math.round(S[3] * 0.7); y++) for (let x = 0; x < S[2]; x++) put(S[0] + x, S[1] + y, shade(hair, (hash(x, y, 6) - 0.5) * 0.15));
          for (let x = 0; x < fw; x++) put(fx + x, fy, shade(hair, -0.05));
          for (const [sx, sy, sw, sh] of [R.west, R.east]) for (let y = 0; y < Math.round(sh * 0.4); y++) for (let x = 0; x < sw; x++) put(sx + x, sy + y, shade(hair, (hash(x, y, 8) - 0.5) * 0.15));
        }
        if (proj.model.kind === "drowned") for (const ex of [lx, rx]) put(ex, ey, hex2rgb(pal.glow));
        if (proj.model.opt?.tattoo) for (let y = 1; y < fh - 1; y += 2) put(fx + 1, fy + y, hex2rgb(pal.accent));
      } else {
        const g = pat === "rockface" ? hex2rgb(pal.glow) : [18, 14, 12];
        put(lx, ey, g); put(rx, ey, g);
      }
    }
  }
}

module.exports={ROSTER,UNDEAD_KINDS,buildTemplate,autoUV,paintModel,clone,part};
