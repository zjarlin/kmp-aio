package site.addzero.kcloud.plugins.hostconfig.service

import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.DeviceDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.FeatureDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.LabelDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.ProductDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.ProductDefinitionLabelLink
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.PropertyDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.by
import site.addzero.kcloud.plugins.hostconfig.model.entity.DataType
import site.addzero.kcloud.plugins.hostconfig.model.entity.Device
import site.addzero.kcloud.plugins.hostconfig.model.entity.DeviceType
import site.addzero.kcloud.plugins.hostconfig.model.entity.ModuleInstance
import site.addzero.kcloud.plugins.hostconfig.model.entity.ModuleTemplate
import site.addzero.kcloud.plugins.hostconfig.model.entity.Project
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProjectMqttConfig
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProjectModbusServerConfig
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProjectProtocol
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProtocolInstance
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProtocolTemplate
import site.addzero.kcloud.plugins.hostconfig.model.entity.RegisterType
import site.addzero.kcloud.plugins.hostconfig.model.entity.Tag
import site.addzero.kcloud.plugins.hostconfig.model.entity.TagValueText
import site.addzero.kcloud.plugins.hostconfig.model.entity.by

/**
 * 集中定义对应内容查询抓取器。
 */
object Fetchers {

    val projectTree: Fetcher<Project> = newFetcher(Project::class).by {
        allScalarFields()
        protocolLinks {
            allScalarFields()
            protocol {
                allScalarFields()
                protocolTemplate {
                    allScalarFields()
                }
                devices {
                    allScalarFields()
                    deviceType {
                        allScalarFields()
                    }
                    modules {
                        allScalarFields()
                        protocol {
                            allScalarFields()
                        }
                        moduleTemplate {
                            allScalarFields()
                        }
                    }
                    tags {
                        allScalarFields()
                    }
                }
            }
        }
    }

    val projectScalar: Fetcher<Project> = newFetcher(Project::class).by {
        allScalarFields()
    }

    val protocolScalar: Fetcher<ProtocolInstance> = newFetcher(ProtocolInstance::class).by {
        allScalarFields()
        protocolTemplate {
            allScalarFields()
        }
    }

    val projectProtocolScalar: Fetcher<ProjectProtocol> = newFetcher(ProjectProtocol::class).by {
        allScalarFields()
        project {
            allScalarFields()
        }
        protocol {
            allScalarFields()
            protocolTemplate {
                allScalarFields()
            }
        }
    }

    val moduleScalar: Fetcher<ModuleInstance> = newFetcher(ModuleInstance::class).by {
        allScalarFields()
        device {
            allScalarFields()
            protocol {
                allScalarFields()
                protocolTemplate {
                    allScalarFields()
                }
            }
        }
        protocol {
            allScalarFields()
            protocolTemplate {
                allScalarFields()
            }
        }
        moduleTemplate {
            allScalarFields()
            protocolTemplate {
                allScalarFields()
            }
        }
    }

    val deviceScalar: Fetcher<Device> = newFetcher(Device::class).by {
        allScalarFields()
        protocol {
            allScalarFields()
            protocolTemplate {
                allScalarFields()
            }
        }
        modules {
            allScalarFields()
        }
        deviceType {
            allScalarFields()
        }
    }

    val tagDetail: Fetcher<Tag> = newFetcher(Tag::class).by {
        allScalarFields()
        device {
            allScalarFields()
        }
        dataType {
            allScalarFields()
        }
        registerType {
            allScalarFields()
        }
        forwardRegisterType {
            allScalarFields()
        }
        valueTexts {
            allScalarFields()
        }
    }

    val mqttConfig: Fetcher<ProjectMqttConfig> = newFetcher(ProjectMqttConfig::class).by {
        allScalarFields()
    }

    val modbusConfig: Fetcher<ProjectModbusServerConfig> = newFetcher(ProjectModbusServerConfig::class).by {
        allScalarFields()
    }

    val protocolTemplate: Fetcher<ProtocolTemplate> = newFetcher(ProtocolTemplate::class).by {
        allScalarFields()
    }

    val moduleTemplate: Fetcher<ModuleTemplate> = newFetcher(ModuleTemplate::class).by {
        allScalarFields()
        protocolTemplate {
            allScalarFields()
        }
    }

    val deviceType: Fetcher<DeviceType> = newFetcher(DeviceType::class).by {
        allScalarFields()
    }

    val registerType: Fetcher<RegisterType> = newFetcher(RegisterType::class).by {
        allScalarFields()
    }

    val dataType: Fetcher<DataType> = newFetcher(DataType::class).by {
        allScalarFields()
    }

    val tagValueText: Fetcher<TagValueText> = newFetcher(TagValueText::class).by {
        allScalarFields()
    }

    val productDefinitionTree: Fetcher<ProductDefinition> = newFetcher(ProductDefinition::class).by {
        allScalarFields()
        labelLinks {
            allScalarFields()
            label {
                allScalarFields()
            }
        }
        devices {
            allScalarFields()
            deviceType {
                allScalarFields()
            }
            properties {
                allScalarFields()
                dataType {
                    allScalarFields()
                }
            }
            features {
                allScalarFields()
            }
        }
    }

    val productDefinitionScalar: Fetcher<ProductDefinition> = newFetcher(ProductDefinition::class).by {
        allScalarFields()
        labelLinks {
            allScalarFields()
            label {
                allScalarFields()
            }
        }
    }

    val deviceDefinitionDetail: Fetcher<DeviceDefinition> = newFetcher(DeviceDefinition::class).by {
        allScalarFields()
        product {
            allScalarFields()
        }
        deviceType {
            allScalarFields()
        }
        properties {
            allScalarFields()
            dataType {
                allScalarFields()
            }
        }
        features {
            allScalarFields()
        }
    }

    val propertyDefinitionDetail: Fetcher<PropertyDefinition> = newFetcher(PropertyDefinition::class).by {
        allScalarFields()
        deviceDefinition {
            allScalarFields()
        }
        dataType {
            allScalarFields()
        }
    }

    val featureDefinitionDetail: Fetcher<FeatureDefinition> = newFetcher(FeatureDefinition::class).by {
        allScalarFields()
        deviceDefinition {
            allScalarFields()
        }
    }

    val labelDefinition: Fetcher<LabelDefinition> = newFetcher(LabelDefinition::class).by {
        allScalarFields()
    }

    val productDefinitionLabelLink: Fetcher<ProductDefinitionLabelLink> = newFetcher(ProductDefinitionLabelLink::class).by {
        allScalarFields()
        product {
            allScalarFields()
        }
        label {
            allScalarFields()
        }
    }
}
