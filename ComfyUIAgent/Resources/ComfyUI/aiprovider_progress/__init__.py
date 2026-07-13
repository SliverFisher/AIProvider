from aiohttp import web
from server import PromptServer
from comfy_execution.progress import get_progress_state


@PromptServer.instance.routes.get("/aiprovider/progress")
async def aiprovider_progress(_request):
    registry = get_progress_state()
    nodes = {}
    for node_id, state in registry.nodes.items():
        status = state.get("state")
        nodes[str(node_id)] = {
            "state": getattr(status, "value", str(status)),
            "value": float(state.get("value", 0)),
            "max": float(state.get("max", 1)),
        }
    return web.json_response({"promptId": registry.prompt_id, "nodes": nodes})


NODE_CLASS_MAPPINGS = {}
NODE_DISPLAY_NAME_MAPPINGS = {}

