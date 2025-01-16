/*
 * Copyright (c) 2022-2024 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.teammoeg.frostedheart.mixin.forge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.util.FHVersion;
import com.teammoeg.chorda.util.io.FileUtil;
import com.teammoeg.chorda.util.io.ZipFile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mixin(ServerLifecycleHooks.class)
public class ServerLifecycleHooksMixin {
    @Shadow(remap = false)
    private static LevelResource SERVERCONFIG;
    private static LevelResource bkfconfig = new LevelResource("serverconfigbackup");
    @Shadow(remap = false)
    private static Logger LOGGER;

    //automatically update serverconfig
    @Inject(at = @At("HEAD"), method = "handleServerAboutToStart", remap = false)
    private static void fh$updateConfig(MinecraftServer server, CallbackInfoReturnable cir) {
        Path config = server.getWorldPath(SERVERCONFIG);
        Path configbkf = server.getWorldPath(bkfconfig);
        FHMain.lastbkf = null;
        FHMain.saveNeedUpdate = false;
        File fconfig = config.toFile();
        File saveVersion = new File(fconfig, ".twrsaveversion");
        FHMain.lastServerConfig = config.toFile();
        FHVersion local = FHMain.local.fetchVersion().orElse(FHVersion.empty);
        String localVersion = local.getOriginal();
        if(!saveVersion.exists())
        	return;
        if (saveVersion.exists() && !localVersion.isEmpty()) {
            try {
                String lw = FileUtil.readString(saveVersion);
                if (!lw.isEmpty() && (lw.equals(localVersion)))
                    return;
                FHMain.saveNeedUpdate = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            LOGGER.info("Making backup for old config files...");
            configbkf.toFile().mkdirs();
            File backup = new File(configbkf.toFile(), "backup-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".zip");
            ZipFile zf = new ZipFile(backup, config);

            zf.addAndDel(config.toFile(), f -> !f.getName().startsWith(".") && f.getName().endsWith(".toml"));

            zf.close();
            fconfig.mkdirs();

            LOGGER.info("Trying to copy new files...");
            File defau = FMLPaths.GAMEDIR.get().resolve("defaultconfigs").toFile();
            for (File f : defau.listFiles(f -> f.getName().endsWith(".toml"))) {
                File nf = new File(fconfig, f.getName());

                FileUtil.transfer(f, nf);
                LOGGER.info("Copied " + f.getName());

            }

            LOGGER.info("Finishing update...");
            FileUtil.transfer(localVersion, saveVersion);
            FHMain.lastbkf = backup;
            FHMain.saveNeedUpdate = false;

            LOGGER.info("Save update succeed, old configuration has been backup to " + backup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
