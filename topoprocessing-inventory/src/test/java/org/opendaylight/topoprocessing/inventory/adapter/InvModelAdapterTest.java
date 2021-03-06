package org.opendaylight.topoprocessing.inventory.adapter;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opendaylight.topoprocessing.impl.adapter.ModelAdapter;
import org.opendaylight.topoprocessing.impl.request.TopologyRequestListener;
import org.opendaylight.topoprocessing.impl.rpc.RpcServices;
import org.opendaylight.topoprocessing.impl.testUtilities.TestingDOMDataBroker;
import org.opendaylight.topoprocessing.impl.util.GlobalSchemaContextHolder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.correlation.rev150121.CorrelationItemEnum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.correlation.rev150121.Model;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;

public class InvModelAdapterTest {

    private InvModelAdapter adapter = new InvModelAdapter();

    @Test
    public void testCreateTopologyRequestListener() {
        TestingDOMDataBroker dataBroker = new TestingDOMDataBroker();
        BindingNormalizedNodeSerializer nodeSerializer = mock(BindingNormalizedNodeSerializer.class);
        GlobalSchemaContextHolder schemaHolder = mock(GlobalSchemaContextHolder.class);
        RpcServices rpcServices = mock(RpcServices.class);
        Map<Class<? extends Model>, ModelAdapter> modelAdapters = new HashMap<>();
        TopologyRequestListener listener = adapter.createTopologyRequestListener(dataBroker, nodeSerializer,
                schemaHolder, rpcServices, modelAdapters);
        assertEquals(0, listener.getTopoRequestHandlers().size());
        // 7 default filtrators are added
        assertEquals(7, listener.getFiltrators().size());
    }

    @Test
    public void testCreateItemIdentifier() {
        InstanceIdentifierBuilder builder = YangInstanceIdentifier.builder();
        YangInstanceIdentifier nodeIdentifier = adapter.buildItemIdentifier(builder, CorrelationItemEnum.Node);
        builder = YangInstanceIdentifier.builder();
        YangInstanceIdentifier tpIdentifier =
                adapter.buildItemIdentifier(builder, CorrelationItemEnum.TerminationPoint);
        assertEquals(nodeIdentifier, tpIdentifier);
        builder = YangInstanceIdentifier.builder();
        YangInstanceIdentifier linkIdentifier = adapter.buildItemIdentifier(builder, CorrelationItemEnum.Link);
        builder = YangInstanceIdentifier.builder();
        assertEquals(builder.node(Link.QNAME).build(), linkIdentifier);
    }
}
