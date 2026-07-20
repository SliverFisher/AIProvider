using System.Text.Json.Nodes;

public static class WorkflowOutputBinding
{
    public static bool IsSupportedOutput(JsonObject node)
    {
        var type = node["class_type"]?.GetValue<string>();
        return type is "SaveImage" or "SaveVideo";
    }

    public static KeyValuePair<string, JsonObject>? FindPrimaryOutput(
        IReadOnlyDictionary<string, JsonObject> entries)
    {
        static bool IsFinalOutput(JsonObject node)
        {
            var title = node["_meta"]?["title"]?.GetValue<string>() ?? "";
            return title.Contains("最终", StringComparison.OrdinalIgnoreCase)
                || title.Contains("final", StringComparison.OrdinalIgnoreCase);
        }

        foreach (var entry in entries)
            if (IsSupportedOutput(entry.Value) && IsFinalOutput(entry.Value)) return entry;
        foreach (var entry in entries)
            if (IsSupportedOutput(entry.Value)) return entry;
        return null;
    }

    public static string? FindOutputNodeId(JsonObject prompt, string? boundNodeId, string outputTitle)
    {
        if (boundNodeId != null && prompt[boundNodeId] is JsonObject boundNode && IsSupportedOutput(boundNode))
            return boundNodeId;

        foreach (var entry in prompt)
            if (entry.Value is JsonObject node && IsSupportedOutput(node)
                && string.Equals(node["_meta"]?["title"]?.GetValue<string>(), outputTitle, StringComparison.Ordinal))
                return entry.Key;
        return null;
    }
}
