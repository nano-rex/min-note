const port = Number(process.env.PORT || 4173);

Bun.serve({
  port,
  async fetch(request) {
    const url = new URL(request.url);
    let path = url.pathname === "/" ? "/index.html" : url.pathname;
    path = path.replace(/^\/+/, "");

    if (path.includes("..")) {
      return new Response("Not found", { status: 404 });
    }

    const file = Bun.file(new URL(`./${path}`, import.meta.url));
    if (!(await file.exists())) {
      return new Response("Not found", { status: 404 });
    }

    return new Response(file);
  }
});

console.log(`min-note web listening on http://localhost:${port}`);
