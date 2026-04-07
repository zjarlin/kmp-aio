package site.addzero.kcloud.plugins.hostconfig.service

import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
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
                modules {
                    allScalarFields()
                    protocol {
                        allScalarFields()
                    }
                    moduleTemplate {
                        allScalarFields()
                    }
                    devices {
                        allScalarFields()
                        deviceType {
                            allScalarFields()
                        }
                        tags {
                            allScalarFields()
                        }
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
        protocol {
            allScalarFields()
        }
        moduleTemplate {
            allScalarFields()
        }
    }

    val deviceScalar: Fetcher<Device> = newFetcher(Device::class).by {
        allScalarFields()
        module {
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
}
