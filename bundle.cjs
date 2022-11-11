const fs = require("fs-extra");
const path = require("node:path");
const esbuild = require("esbuild");

const main = async () => {
  const squint = await import("squint-cljs");

  const buf = fs.readFileSync(path.join("src", "wordle.cljs"));
  const app_js = squint.compileString(buf.toString());

  // https://esbuild.github.io/api/#target
  const target = ["chrome100", "firefox100"];

  const result = await esbuild.build({
    bundle: true,
    // If we use a hash in the entry point name, we have to use a placeholder in
    // the index.html (and in the source map), and replace the placeholder with
    // the entry point name after esbuild has finished.
    // entryNames: "[dir]/[name]-[hash]",
    entryPoints: {
      app: path.join("src", "wordle.mjs"),
    },
    format: "esm",
    metafile: true,
    minify: true,
    outdir: "public",
    sourcemap: true,
    target,
    write: true,
  });

  console.log(result);

  let text = await esbuild.analyzeMetafile(result.metafile, {
    color: true,
    verbose: true,
  });

  console.log(text);

  await fs.writeFile(
    path.join("public", "metafile-js.json"),
    JSON.stringify(result.metafile)
  );

  const result_css = await esbuild.build({
    bundle: true,
    // entryNames: "[dir]/[name]-[hash]",
    entryPoints: {
      app: path.join("assets", "style.css"),
    },
    loader: { ".css": "css" },
    metafile: true,
    minify: true,
    outdir: "public",
    sourcemap: true,
    target,
    write: true,
  });

  text = await esbuild.analyzeMetafile(result_css.metafile, {
    color: true,
    verbose: true,
  });

  console.log(text);

  await fs.writeFile(
    path.join("public", "metafile-css.json"),
    JSON.stringify(result_css.metafile)
  );
};

main();
