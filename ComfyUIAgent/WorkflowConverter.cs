using System.Text.Json.Nodes;

static class WorkflowConverter
{
    private static readonly HashSet<string> PrimitiveWidgetTypes = new(StringComparer.OrdinalIgnoreCase)
    {
        "BOOLEAN", "FLOAT", "INT", "NUMBER", "STRING"
    };

    public static JsonObject Convert(JsonObject source, JsonObject objectInfo)
    {
        var sourceNodes = source["nodes"] as JsonArray
            ?? throw new InvalidOperationException("界面工作流缺少 nodes");
        var links = ReadLinks(source["links"] as JsonArray);
        var prompt = new JsonObject();

        foreach (var sourceNode in sourceNodes.OfType<JsonObject>())
        {
            var id = sourceNode["id"]?.ToString();
            var type = sourceNode["type"]?.GetValue<string>();
            if (string.IsNullOrWhiteSpace(id) || string.IsNullOrWhiteSpace(type)) continue;

            var mode = sourceNode["mode"]?.GetValue<int>() ?? 0;
            if (mode != 0)
                throw new InvalidOperationException($"节点 {id}（{type}）处于停用或旁路状态，macOS 本地转换暂不支持该状态");

            if (objectInfo[type] is not JsonObject nodeInfo)
            {
                // Notes and reroutes are UI-only and never appear in an API prompt.
                if (type.Equals("Note", StringComparison.OrdinalIgnoreCase) ||
                    type.Contains("Reroute", StringComparison.OrdinalIgnoreCase)) continue;
                throw new InvalidOperationException($"ComfyUI 未返回节点类型 {type} 的定义");
            }

            var apiInputs = new JsonObject();
            var serializedInputs = (sourceNode["inputs"] as JsonArray)?.OfType<JsonObject>()
                .Where(input => input["name"] != null)
                .ToDictionary(input => input["name"]!.ToString(), StringComparer.Ordinal) ?? new Dictionary<string, JsonObject>();
            var widgetValues = sourceNode["widgets_values"] as JsonArray ?? new JsonArray();
            var widgetIndex = 0;

            foreach (var (name, spec) in InputDefinitions(nodeInfo))
            {
                serializedInputs.TryGetValue(name, out var serializedInput);
                var linkId = serializedInput?["link"]?.ToString();
                if (!string.IsNullOrWhiteSpace(linkId) && links.TryGetValue(linkId, out var link))
                {
                    apiInputs[name] = new JsonArray(link.OriginNodeId, link.OriginSlot);
                    continue;
                }

                if (!IsWidget(spec, serializedInput)) continue;
                if (widgetIndex >= widgetValues.Count)
                    throw new InvalidOperationException($"节点 {id}（{type}）缺少控件值 {name}");

                apiInputs[name] = widgetValues[widgetIndex++]?.DeepClone();
                if (HasControlAfterGenerate(spec) && widgetIndex < widgetValues.Count) widgetIndex++;
            }

            var title = sourceNode["title"]?.GetValue<string>()
                ?? sourceNode["properties"]?["Node name for S&R"]?.GetValue<string>()
                ?? type;
            prompt[id] = new JsonObject
            {
                ["class_type"] = type,
                ["inputs"] = apiInputs,
                ["_meta"] = new JsonObject { ["title"] = title }
            };
        }

        if (prompt.Count == 0) throw new InvalidOperationException("界面工作流没有可执行节点");
        return prompt;
    }

    private static IEnumerable<(string Name, JsonNode? Spec)> InputDefinitions(JsonObject nodeInfo)
    {
        if (nodeInfo["input"] is not JsonObject input) yield break;
        foreach (var groupName in new[] { "required", "optional" })
            if (input[groupName] is JsonObject group)
                foreach (var entry in group) yield return (entry.Key, entry.Value);
    }

    private static bool IsWidget(JsonNode? spec, JsonObject? serializedInput)
    {
        if (serializedInput?["widget"] is JsonObject) return true;
        if (spec is not JsonArray definition || definition.Count == 0) return false;
        if (definition[0] is JsonArray) return true;
        var type = definition[0]?.GetValue<string>();
        if (type != null && PrimitiveWidgetTypes.Contains(type)) return true;
        return definition.Count > 1 && definition[1]?["default"] != null && definition[1]?["forceInput"]?.GetValue<bool>() != true;
    }

    private static bool HasControlAfterGenerate(JsonNode? spec) =>
        spec is JsonArray { Count: > 1 } definition && definition[1]?["control_after_generate"]?.GetValue<bool>() == true;

    private static Dictionary<string, WorkflowLink> ReadLinks(JsonArray? sourceLinks)
    {
        var result = new Dictionary<string, WorkflowLink>(StringComparer.Ordinal);
        if (sourceLinks == null) return result;
        foreach (var link in sourceLinks)
        {
            if (link is JsonArray { Count: >= 4 } cells && cells[0] != null && cells[1] != null && cells[2] != null)
            {
                result[cells[0]!.ToString()] = new WorkflowLink(cells[1]!.ToString(), cells[2]!.GetValue<int>());
                continue;
            }
            if (link is JsonObject item && item["id"] != null && item["origin_id"] != null && item["origin_slot"] != null)
                result[item["id"]!.ToString()] = new WorkflowLink(item["origin_id"]!.ToString(), item["origin_slot"]!.GetValue<int>());
        }
        return result;
    }

    private sealed record WorkflowLink(string OriginNodeId, int OriginSlot);
}
