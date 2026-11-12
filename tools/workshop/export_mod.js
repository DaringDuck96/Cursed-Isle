const fs = require("fs"), zlib = require("zlib"), path = require("path");
const L = require("./gen_lib.js");
const MOD = "/home/claude/cursed-tides/src/main/";
const TEX = MOD + "resources/assets/cursedtides/textures/entity/workshop/";
fs.mkdirSync(TEX, { recursive: true });

function png(W, H, data) {
  const crcT = []; for (let n = 0; n < 256; n++) { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; crcT[n] = c >>> 0; }
  const crc = (buf) => { let c = 0xffffffff; for (const b of buf) c = crcT[(c ^ b) & 255] ^ (c >>> 8); return (c ^ 0xffffffff) >>> 0; };
  const chunk = (type, body) => { const len = Buffer.alloc(4); len.writeUInt32BE(body.length); const tb = Buffer.concat([Buffer.from(type), body]); const c = Buffer.alloc(4); c.writeUInt32BE(crc(tb)); return Buffer.concat([len, tb, c]); };
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
  const raw = Buffer.alloc((W * 4 + 1) * H);
  for (let y = 0; y < H; y++) { raw[y * (W * 4 + 1)] = 0; Buffer.from(data.buffer, y * W * 4, W * 4).copy(raw, y * (W * 4 + 1) + 1); }
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk("IHDR", ihdr), chunk("IDAT", zlib.deflateSync(raw, { level: 9 })), chunk("IEND", Buffer.alloc(0))]);
}
const f = (v) => { const r = Math.round(v * 1000) / 1000; return (Number.isInteger(r) ? r + ".0" : String(r)) + "f"; };
function java(id, m) {
  const lines = [], vars = {}; let k = 0;
  const ordered = []; const visit = (parent) => { for (const p of m.parts.filter(x => (x.parent || null) === parent)) { ordered.push(p); visit(p.name); } }; visit(null);
  for (const p of ordered) {
    const v = "p" + (k++); vars[p.name] = v;
    let chain = "ModelPartBuilder.create()", mir = false;
    for (const c of p.cubes) {
      if (!!c.mirror !== mir) { chain += c.mirror ? ".mirrored()" : ".mirrored(false)"; mir = !!c.mirror; }
      chain += `.uv(${c.uv[0]}, ${c.uv[1]}).cuboid(${c.o.map(f).join(", ")}, ${c.s.map(f).join(", ")}${c.inflate ? `, new Dilation(${f(c.inflate)})` : ""})`;
    }
    const rad = p.rot.map(d => d * Math.PI / 180);
    const tr = rad.every(r => r === 0) ? `ModelTransform.pivot(${p.pivot.map(f).join(", ")})` : `ModelTransform.of(${p.pivot.map(f).join(", ")}, ${rad.map(f).join(", ")})`;
    lines.push(`        ModelPartData ${v} = ${p.parent && vars[p.parent] ? vars[p.parent] : "root"}.addChild("${p.name}", ${chain}, ${tr});`);
  }
  const method = id.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
  return { method, code: `    public static TexturedModelData ${method}() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
${lines.join("\n")}
        return TexturedModelData.of(data, ${m.tex[0]}, ${m.tex[1]});
    }
` };
}
function paint(proj, file) {
  const [W, H] = proj.model.tex;
  const img = { data: new Uint8ClampedArray(W * H * 4) };
  L.paintModel(proj, img);
  fs.writeFileSync(TEX + file + ".png", png(W, H, img.data));
}
const byId = Object.fromEntries(L.ROSTER.map(e => [e.id, e]));
const methods = [], info = [];
function model(id, outId) {
  const e = byId[id];
  const proj = L.buildTemplate(e);
  if (e.tpl === "biped" && !proj.model.parts.some(p => p.name === "hat")) proj.model.parts.push(L.part("hat", null, [0, 0, 0], [0, 0, 0], []));
  L.autoUV(proj.model);
  const j = java(outId || id, proj.model);
  methods.push(j); info.push([outId || id, j.method, proj.model.tex.join("x")]);
  return proj;
}
// People and the drowned captain: one model and texture each
const PEOPLE = ["keeper_wren","ysolde","tamsin","vey","galloway","harrow","rook","thwaite","wayfarer","quill","isadora","celestine","sorrel","ibbie","dagny","old_mariner",
  "mags","jory","dealer","fairweather","pip","lott","finch","grista","ned_flint","bess","skip","deedes","marsh","sounding","kettle","flossie","moss",
  "quartermaster","shipwright","marine","crew_gunner","crew_bosun","crew_navigator","bounty_hunter","thorne"];
for (const id of PEOPLE) paint(model(id), id);
// The cursed: three builds, four kinds of each
for (const rank of ["cursed_grunt", "cursed_brute", "cursed_captain"]) {
  const proj = model(rank);
  const base = byId[rank].pal;
  for (const [kind, over] of Object.entries(L.UNDEAD_KINDS)) {
    proj.palette = Object.assign(L.clone(base), over);
    if (rank === "cursed_captain") { proj.palette.coat = base.coat; proj.palette.hat = base.hat; }
    paint(proj, rank + "_" + kind.toLowerCase());
  }
}
// Ships: each class in plain, navy and ghost colours
const shipInfo = {};
for (const cls of ["sloop","schooner","brigantine","brig","frigate","galleon","ship_of_the_line"]) {
  const proj = model("ship_" + cls);
  const e = byId["ship_" + cls];
  for (const [look, key] of [["plain", "Player"], ["navy", "Navy"], ["ghost", "Ghost"]]) {
    proj.palette = Object.assign(L.clone(e.pal), e.variants[key]);
    paint(proj, "ship_" + cls + "_" + look);
  }
  const sails = proj.model.parts.filter(p => p.name.startsWith("sail")).length;
  const bow = proj.model.parts.find(p => p.name === "figurehead").cubes[0];
  shipInfo[cls] = { sails, fig: [bow.o[1] + 3, bow.o[2] + 1.5] };
}
const out = `package com.cursedtides.client;

import net.minecraft.client.model.*;

/**
 * Models from the Cursed Tides Mob Workshop (its starting designs), generated; textures are in
 * textures/entity/workshop/. Regenerate rather than editing by hand.
 */
public final class WorkshopModels {
    private WorkshopModels() {}

    public static net.minecraft.client.render.entity.model.EntityModelLayer layer(String id) {
        return new net.minecraft.client.render.entity.model.EntityModelLayer(com.cursedtides.CursedTides.id("workshop_" + id), "main");
    }

    public static void registerLayers() {
${info.map(([id, m]) => `        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("${id}"), WorkshopModels::${m});`).join("\n")}
    }

${methods.map(m => m.code).join("\n")}}
`;
fs.writeFileSync(MOD + "java/com/cursedtides/client/WorkshopModels.java", out);
fs.writeFileSync("export_info.json", JSON.stringify({ info, shipInfo }, null, 1));
console.log(info.length, "models;", fs.readdirSync(TEX).length, "textures;", Math.round(fs.readdirSync(TEX).reduce((a, x) => a + fs.statSync(TEX + x).size, 0) / 1024), "KB");
console.log(JSON.stringify(shipInfo));
