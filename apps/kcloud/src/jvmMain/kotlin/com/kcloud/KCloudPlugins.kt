package com.kcloud

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugins.desktop.DesktopIntegrationPluginBundle
import com.kcloud.plugins.dotfiles.DotfilesPluginBundle
import com.kcloud.plugins.dotfiles.server.DotfilesServerPluginBundle
import com.kcloud.plugins.environment.EnvironmentPluginBundle
import com.kcloud.plugins.environment.server.EnvironmentServerPluginBundle
import com.kcloud.plugins.file.FilePluginBundle
import com.kcloud.plugins.file.server.FileServerPluginBundle
import com.kcloud.plugins.notes.NotesPluginBundle
import com.kcloud.plugins.notes.server.NotesServerPluginBundle
import com.kcloud.plugins.packages.PackageOrganizerPluginBundle
import com.kcloud.plugins.packages.server.PackageOrganizerServerPluginBundle
import com.kcloud.plugins.quicktransfer.QuickTransferPluginBundle
import com.kcloud.plugins.quicktransfer.server.QuickTransferServerPluginBundle
import com.kcloud.plugins.servermanagement.ServerManagementPluginBundle
import com.kcloud.plugins.servermanagement.server.ServerManagementServerPluginBundle
import com.kcloud.plugins.ssh.SshPluginBundle
import com.kcloud.plugins.ssh.server.SshServerPluginBundle
import com.kcloud.plugins.settings.SettingsPluginBundle
import com.kcloud.plugins.transferhistory.TransferHistoryPluginBundle
import com.kcloud.plugins.transferhistory.server.TransferHistoryServerPluginBundle
import com.kcloud.plugins.webdav.WebDavPluginBundle
import com.kcloud.plugins.webdav.server.WebDavServerPluginBundle

val allKCloudPluginBundles: List<KCloudPluginBundle> = listOf(
    DesktopIntegrationPluginBundle,
    QuickTransferPluginBundle,
    QuickTransferServerPluginBundle,
    ServerManagementPluginBundle,
    ServerManagementServerPluginBundle,
    FilePluginBundle,
    FileServerPluginBundle,
    NotesPluginBundle,
    NotesServerPluginBundle,
    PackageOrganizerPluginBundle,
    PackageOrganizerServerPluginBundle,
    SshPluginBundle,
    SshServerPluginBundle,
    TransferHistoryPluginBundle,
    TransferHistoryServerPluginBundle,
    WebDavPluginBundle,
    WebDavServerPluginBundle,
    DotfilesPluginBundle,
    DotfilesServerPluginBundle,
    EnvironmentPluginBundle,
    EnvironmentServerPluginBundle,
    SettingsPluginBundle
)
