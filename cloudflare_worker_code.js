addEventListener("fetch", (event) => {
    event.respondWith(
        handleRequest(event).catch(
            (err) => new Response(err.stack, { status: 500 })
        )
    );
});


async function trackEvent(apiKey, sampling){
    const logRequest = {
        body: JSON.stringify(
            {
                apiKey: apiKey,
                sampling: sampling
            }
        ),
        method: "POST",
        headers: {
            "Authorization": SERVER_ANALYTICS_TOKEN,
            "content-type": "application/json",
        }
    }
    const url = `${SERVER_ANALYTICS_URL}/api/analytics/tracked-sessions`
    await fetch(url, logRequest)
}


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

    let result
    if (config.lastModification === lastModifClient){
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

    const sampling = 100.0
    if (Math.random() < 1.0/sampling){
        event.waitUntil(trackEvent(apiKey, sampling))
    }

    return new Response(JSON.stringify(result), {
        headers: {
            "Access-Control-Allow-Origin": "*",
            "Content-Type": "application/json"
        },
    });

}
