using System.Text.Json.Nodes;
using Xunit;

public sealed class WorkflowOutputBindingTests
{
    [Fact]
    public void FindPrimaryOutput_SelectsFinalSaveVideoWithFilenamePrefix()
    {
        var entries = new Dictionary<string, JsonObject>
        {
            ["13"] = JsonNode.Parse("""{"class_type":"VAEDecode","inputs":{}}""")!.AsObject(),
            ["15"] = JsonNode.Parse("""{"class_type":"SaveVideo","_meta":{"title":"SAVE_FINAL_VIDEO"},"inputs":{"filename_prefix":"video/Wan2.2_I2V_Q4KM","format":"mp4"}}""")!.AsObject()
        };

        var output = WorkflowOutputBinding.FindPrimaryOutput(entries);

        Assert.NotNull(output);
        Assert.Equal("15", output.Value.Key);
        Assert.Equal("video/Wan2.2_I2V_Q4KM", output.Value.Value["inputs"]?["filename_prefix"]?.GetValue<string>());
    }

    [Fact]
    public void FindOutputNodeId_AcceptsBoundSaveVideoNode()
    {
        var prompt = JsonNode.Parse("""{"15":{"class_type":"SaveVideo","_meta":{"title":"SAVE_FINAL_VIDEO"},"inputs":{"filename_prefix":"video/test"}}}""")!.AsObject();

        var nodeId = WorkflowOutputBinding.FindOutputNodeId(prompt, "15", "SAVE_FINAL_VIDEO");

        Assert.Equal("15", nodeId);
    }
}
