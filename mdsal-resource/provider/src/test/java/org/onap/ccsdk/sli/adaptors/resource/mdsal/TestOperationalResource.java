package org.onap.ccsdk.sli.adaptors.resource.mdsal;

import static org.mockito.Mockito.mock;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import junit.framework.TestCase;

public class TestOperationalResource extends TestCase {


	public void test() throws Exception {

		RestService restService = mock(RestService.class);
		SvcLogicContext ctx = new SvcLogicContext();

		OperationalResource res = new OperationalResource(restService);

		res.delete("my-resource", null, ctx);
		res.notify("my-resource", "action", "key", ctx);
		res.query("my-resource", false, "my-select", "mykey", "pfx", null, ctx);
		res.release("my-resource", "mykey", ctx);
		res.reserve("my-resource", "my-select", "mykey", "pfx", ctx);
		res.exists("my-resource", "mykey", "pfx", ctx);
		res.isAvailable("my-resource", "mykey", "pfx", ctx);
		res.save("resource", false, false, null, null, null, ctx);
		res.update("my-resource", "mykey", null, "pfx", ctx);
	}
}
