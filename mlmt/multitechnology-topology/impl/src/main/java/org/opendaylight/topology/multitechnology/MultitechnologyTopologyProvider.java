/*
 * Copyright (c) 2015 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.topology.multitechnology;

import com.google.common.base.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.TopologyTypes;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.ted.rev131021.TedNodeAttributes;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.ted.rev131021.TedLinkAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.MtTopologyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.MtTopologyTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.mt.info.attribute.ValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.mt.info.AttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.mt.info.Attribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.mt.info.AttributeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.multitechnology.topology.type.MultitechnologyTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.MtInfoLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.MtInfoLink;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.MtInfoNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.rev150122.MtInfoNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.NativeL3IgpMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.NativeTed;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.MtTedNodeAttributeValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.MtTedNodeAttributeValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.MtTedLinkAttributeValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.MtTedLinkAttributeValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.MtLinkMetricAttributeValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.ted.rev150122.MtLinkMetricAttributeValueBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.topology.mlmt.utility.MlmtOperationProcessor;
import org.opendaylight.topology.mlmt.utility.MlmtTopologyOperation;
import org.opendaylight.topology.mlmt.utility.MlmtTopologyProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.topology.multitechnology.impl.rev150122.MultitechnologyTopologyProviderRuntimeMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultitechnologyTopologyProvider implements MultitechnologyTopologyProviderRuntimeMXBean,
            AutoCloseable, MlmtTopologyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MultitechnologyTopologyProvider.class);
    private DataBroker dataProvider;
    private MlmtOperationProcessor processor;
    private InstanceIdentifier<Topology> destTopologyId;
    private MultitechnologyAttributesParser parser;

    public void init(MlmtOperationProcessor processor,
        final InstanceIdentifier<Topology> destTopologyId, final MultitechnologyAttributesParser parser) {
        LOG.info("MultitechnologyTopologyProvider.init");
        this.destTopologyId = destTopologyId;
        this.processor = processor;
        this.parser = parser;
    }

    public void setDataProvider(DataBroker dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void close() {

    }

    @Override
    public void onTopologyCreated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Topology topology) {
        LOG.info("MultitechnologyTopologyProvider.onTopologyCreated");
        final InstanceIdentifier<Topology> targetTopologyId = destTopologyId;

        processor.enqueueOperation(new MlmtTopologyOperation() {
            @Override
            public void applyOperation(ReadWriteTransaction transaction) {
                 final MultitechnologyTopologyBuilder multitechnologyTopologyBuilder =
                         new MultitechnologyTopologyBuilder();
                 final MtTopologyTypeBuilder mtTopologyTypeBuilder = new MtTopologyTypeBuilder();
                 mtTopologyTypeBuilder.setMultitechnologyTopology(multitechnologyTopologyBuilder.build());
                 InstanceIdentifier<MtTopologyType> target = targetTopologyId.child(TopologyTypes.class)
                         .augmentation(MtTopologyType.class);
                 MtTopologyType top = mtTopologyTypeBuilder.build();
                 transaction.merge(LogicalDatastoreType.OPERATIONAL, target, top, true);
            }
        });
    }

    @Override
    public void onNodeCreated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Node node) {
        handleNodeAttributes(type, topologyInstanceId, node);
    }

    @Override
    public void onTpCreated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final NodeKey nodeKey,
            final TerminationPoint tp) {
        LOG.info("MultitechnologyTopologyProvider.onTpCreated");
    }

    @Override
    public void onLinkCreated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Link link) {
        handleLinkAttributes(type, topologyInstanceId, link);
    }

    @Override
    public void onTopologyUpdated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Topology topology) {
    }

    @Override
    public void onNodeUpdated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Node node) {
        handleNodeAttributes(type, topologyInstanceId, node);
    }

    @Override
    public void onTpUpdated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final NodeKey nodeKey,
            final TerminationPoint tp) {
    }

    @Override
    public void onLinkUpdated(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Link link) {
        handleLinkAttributes(type, topologyInstanceId, link);
    }

    @Override
    public void onTopologyDeleted(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId) {

    }

    @Override
    public void onNodeDeleted(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final NodeKey nodeKey) {

    }

    @Override
    public void onTpDeleted(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final NodeKey nodeKey,
            final TerminationPointKey tpKey) {

    }

    @Override
    public void onLinkDeleted(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final LinkKey linkKey) {

    }

    private void handleNodeAttributes(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Node node) {
        LOG.info("MultitechnologyTopologyProvider.handleNodeAttributes type {} topologyInstanceId {} nodeKey {}",
                type, topologyInstanceId.toString(), node.getKey().toString());
        TedNodeAttributes tedNodeAttributes = parser.parseTedNodeAttributes(node);
        if (tedNodeAttributes == null) {
            return;
        }
        setNativeMtNodeAttributes(LogicalDatastoreType.OPERATIONAL, destTopologyId,
                tedNodeAttributes, node.getKey());
    }

    private void handleLinkAttributes(final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Link link) {
        LOG.info("MultitechnologyTopologyProvider.handleLinkAttributes type {} topologyInstanceId {} linkKey {}",
                type, topologyInstanceId.toString(), link.getKey().toString());
        Long metric = parser.parseLinkMetric(link);
        if (metric != null) {
            setNativeMtLinkMetricAttribute(LogicalDatastoreType.OPERATIONAL, destTopologyId,
                    metric, link.getKey());
        }
        TedLinkAttributes tedLinkAttributes = parser.parseTedLinkAttributes(link);
        if (tedLinkAttributes == null) {
            return;
        }
        setNativeMtLinkTedAttribute(LogicalDatastoreType.OPERATIONAL, destTopologyId,
                tedLinkAttributes, link.getKey());
    }

    private void setNativeMtLinkMetricAttribute(
            final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final Long metric,
            final LinkKey linkKey) {
        try {
            LOG.info("MultitechnologyTopologyProvider.setNativeMtLinkMetricAttribute type {} topology {} linkKey {}",
                type, topologyInstanceId.toString(), linkKey.toString());
            final InstanceIdentifier<Topology> targetTopologyId = topologyInstanceId;
            final String path = "native-l3-igp-metric:1";
            final ReadOnlyTransaction rx = dataProvider.newReadOnlyTransaction();
            final Uri uri = new Uri(path);
            final AttributeKey attributeKey = new AttributeKey(uri);
            final InstanceIdentifier<Attribute> instanceAttributeId = targetTopologyId.child(Link.class, linkKey).
                augmentation(MtInfoLink.class).child(Attribute.class, attributeKey);
            final Optional<Attribute> sourceAttributeObject =
                rx.read(LogicalDatastoreType.OPERATIONAL, instanceAttributeId).get();

            final MtLinkMetricAttributeValueBuilder mtLinkMetricAVBuilder = new MtLinkMetricAttributeValueBuilder();
            mtLinkMetricAVBuilder.setMetric(metric);
            final ValueBuilder valueBuilder = new ValueBuilder();
            valueBuilder.addAugmentation(MtLinkMetricAttributeValue.class, mtLinkMetricAVBuilder.build());
            final AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setAttributeType(NativeL3IgpMetric.class);
            attributeBuilder.setValue(valueBuilder.build());
            attributeBuilder.setId(uri);
            attributeBuilder.setKey(attributeKey);

            processor.enqueueOperation(new MlmtTopologyOperation() {
                @Override
                public void applyOperation(ReadWriteTransaction transaction) {
                    if (sourceAttributeObject != null && sourceAttributeObject.isPresent()
                            && sourceAttributeObject.get() != null) {
                        transaction.put(LogicalDatastoreType.OPERATIONAL,
                                instanceAttributeId, attributeBuilder.build());
                    } else {
                        final MtInfoLinkBuilder mtInfoLinkBuilder = new MtInfoLinkBuilder();
                        final List<Attribute> la = new ArrayList<Attribute>();
                        la.add(attributeBuilder.build());
                        mtInfoLinkBuilder.setAttribute(la);
                        final InstanceIdentifier<MtInfoLink> instanceId =
                                targetTopologyId.child(Link.class, linkKey).
                            augmentation(MtInfoLink.class);
                        transaction.merge(LogicalDatastoreType.OPERATIONAL,
                                instanceId, mtInfoLinkBuilder.build(), true);
                    }
                }
            });
        } catch (final InterruptedException e) {
            LOG.error("MultitechnologyTopologyProvider.setNativeMtLinkMetricAttribute interrupted exception", e);
        } catch (final ExecutionException e) {
            LOG.error("MultitechnologyTopologyProvider.setNativeMtLinkMetricAttribute execution exception", e);
        }
    }

    private void setNativeMtLinkTedAttribute(
            final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final TedLinkAttributes ted,
            final LinkKey linkKey) {
        try {
            LOG.info("MultitechnologyTopologyProvider.setNativeMtLinkTedAttribute type {} topology {} linkKey {}", 
                    type, topologyInstanceId.toString(), linkKey.toString());
            final String path = "native-ted:1";
            final InstanceIdentifier<Topology> targetTopologyId = topologyInstanceId;
            final ReadOnlyTransaction rx = dataProvider.newReadOnlyTransaction();
            final Uri uri = new Uri(path);
            final AttributeKey attributeKey = new AttributeKey(uri);
            final InstanceIdentifier<Attribute> instanceAttributeId = targetTopologyId.child(Link.class, linkKey).
                augmentation(MtInfoLink.class).child(Attribute.class, attributeKey);
            final Optional<Attribute> sourceAttributeObject =
                rx.read(LogicalDatastoreType.OPERATIONAL, instanceAttributeId).get();

            final MtTedLinkAttributeValueBuilder mtTedLAVBuilder = new MtTedLinkAttributeValueBuilder();
            if (ted.getMaxLinkBandwidth() != null) {
                mtTedLAVBuilder.setMaxLinkBandwidth(ted.getMaxLinkBandwidth());
            }
            if (ted.getMaxResvLinkBandwidth() != null) {
                mtTedLAVBuilder.setMaxResvLinkBandwidth(ted.getMaxResvLinkBandwidth());
            }
            if (ted.getUnreservedBandwidth() != null) {
                mtTedLAVBuilder.setUnreservedBandwidth(ted.getUnreservedBandwidth());
            }
            if (ted.getColor() != null) {
                mtTedLAVBuilder.setColor(ted.getColor());
            }
            if (ted.getSrlg() != null) {
                mtTedLAVBuilder.setSrlg(ted.getSrlg());
            }
            if (ted.getTeDefaultMetric() != null) {
                mtTedLAVBuilder.setTeDefaultMetric(ted.getTeDefaultMetric());
            }
            final ValueBuilder valueBuilder = new ValueBuilder();
            valueBuilder.addAugmentation(MtTedLinkAttributeValue.class, mtTedLAVBuilder.build());
            final AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setAttributeType(NativeTed.class);
            attributeBuilder.setValue(valueBuilder.build());
            attributeBuilder.setId(uri);
            attributeBuilder.setKey(attributeKey);

            processor.enqueueOperation(new MlmtTopologyOperation() {
                @Override
                public void applyOperation(ReadWriteTransaction transaction) {
                    if (sourceAttributeObject != null && sourceAttributeObject.isPresent()
                            && sourceAttributeObject.get() != null) {
                        transaction.put(LogicalDatastoreType.OPERATIONAL,
                                instanceAttributeId, attributeBuilder.build());
                    } else {
                        final MtInfoLinkBuilder mtInfoLinkBuilder = new MtInfoLinkBuilder();
                        final List<Attribute> la = new ArrayList<Attribute>();
                        la.add(attributeBuilder.build());
                        mtInfoLinkBuilder.setAttribute(la);
                        final InstanceIdentifier<MtInfoLink> instanceId =
                                targetTopologyId.child(Link.class, linkKey)
                                .augmentation(MtInfoLink.class);
                        transaction.merge(LogicalDatastoreType.OPERATIONAL,
                                instanceId, mtInfoLinkBuilder.build(), true);
                    }
                }
            });
        } catch (final InterruptedException e) {
            LOG.error("MultitechnologyTopologyProvider.setNativeMtLinkTedAttribute interrupted exception", e);
        } catch (final ExecutionException e) {
            LOG.error("MultitechnologyTopologyProvider.setNativeMtLinkTedAttribute execution exception", e);
        }
    }

    private void setNativeMtNodeAttributes(
            final LogicalDatastoreType type,
            final InstanceIdentifier<Topology> topologyInstanceId,
            final TedNodeAttributes ted,
            final NodeKey nodeKey) {
        try {
            LOG.info("MultitechnologyTopologyProvider.setNativeMtNodeAttributes type {} topologyId {} nodeKey {}",
                    type, topologyInstanceId.toString(), nodeKey.toString());
            final InstanceIdentifier<Topology> targetTopologyId = topologyInstanceId;
            final String path = "native-ted:1";
            final ReadOnlyTransaction rx = dataProvider.newReadOnlyTransaction();
            final Uri uri = new Uri(path);
            final AttributeKey attributeKey = new AttributeKey(uri);
            final InstanceIdentifier<Attribute> instanceAttributeId = targetTopologyId.child(Node.class, nodeKey).
                    augmentation(MtInfoNode.class).child(Attribute.class, attributeKey);
            final Optional<Attribute> sourceAttributeObject =
                    rx.read(LogicalDatastoreType.OPERATIONAL, instanceAttributeId).get();

            final MtTedNodeAttributeValueBuilder tedNodeAttrValueBuilder = new MtTedNodeAttributeValueBuilder();
            if (ted.getTeRouterIdIpv4() != null) {
                tedNodeAttrValueBuilder.setTeRouterIdIpv4(ted.getTeRouterIdIpv4());
            }
            if (ted.getTeRouterIdIpv6() != null) {
                tedNodeAttrValueBuilder.setTeRouterIdIpv6(ted.getTeRouterIdIpv6());
            }
            if (ted.getIpv4LocalAddress() != null) {
                tedNodeAttrValueBuilder.setIpv4LocalAddress(ted.getIpv4LocalAddress());
            }
            if (ted.getIpv6LocalAddress() != null) {
                tedNodeAttrValueBuilder.setIpv6LocalAddress(ted.getIpv6LocalAddress());
            }
            if (ted.getPccCapabilities() != null) {
                tedNodeAttrValueBuilder.setPccCapabilities(ted.getPccCapabilities());
            }
            final ValueBuilder valueBuilder = new ValueBuilder();
            valueBuilder.addAugmentation(MtTedNodeAttributeValue.class, tedNodeAttrValueBuilder.build());
            final AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setAttributeType(NativeTed.class);
            attributeBuilder.setValue(valueBuilder.build());
            attributeBuilder.setKey(attributeKey);

            processor.enqueueOperation(new MlmtTopologyOperation() {
                @Override
                public void applyOperation(ReadWriteTransaction transaction) {
                    if (sourceAttributeObject != null && sourceAttributeObject.isPresent()
                            && sourceAttributeObject.get() != null) {
                        transaction.put(LogicalDatastoreType.OPERATIONAL,
                                instanceAttributeId, attributeBuilder.build());
                    } else {
                        final MtInfoNodeBuilder mtInfoNodeBuilder = new MtInfoNodeBuilder();
                        final List<Attribute> la = new ArrayList<Attribute>();
                        la.add(attributeBuilder.build());
                        mtInfoNodeBuilder.setAttribute(la);
                        final InstanceIdentifier<MtInfoNode> instanceId = 
                                targetTopologyId.child(Node.class, nodeKey).augmentation(MtInfoNode.class);
                        transaction.merge(LogicalDatastoreType.OPERATIONAL, instanceId,
                                mtInfoNodeBuilder.build(), true);
                    }
                }
            });
         } catch (final InterruptedException e) {
             LOG.error("MultitechnologyTopologyProvider.setNativeMtNodeAttributes interrupted exception", e);
         } catch (final ExecutionException e) {
             LOG.error("MultitechnologyTopologyProvider.setNativeMtNodeAttributes execution exception", e);
         }
    }
}
