addEventListener("fetch", (event) => {
    event.respondWith(
        handleRequest(event).catch(
            (err) => new Response(err.stack, { status: 500 })
        )
    );
});


async function handleRequest(event) {
    const { request } = event;
    const { pathname, searchParams } = new URL(request.url);
    const lastModifClient = searchParams.get("lastModification")

    const apiKey = pathname.substring(1) // remove the "/" from pathname

    if (!apiKey){
        return new Response('Empty api key', { status: 404 })
    }

    const config = await configs.get(apiKey, {type: "json"})

    if (config == null){
        return new Response(`Unknown api key '${apiKey}'`, { status: 404 })
    }

    const lastModif = config.lastModification

    let result
    if (lastModif === lastModifClient){
        result = {
            modified: false,
            config: null
        }
    } else { // lastModifClient null or different
        result = {
            modified: true,
            config: config
        }
    }

    return new Response(JSON.stringify(result), {
        headers: { "Content-Type": "application/json" },
    });

}
