var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

function initializeCoreMod() {
    return {
        'getRender': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.EntityRenderDispatcher',
                'methodName': ASMAPI.mapMethod('m_114382_'),
                'methodDesc': '(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;'
            },
            'transformer': function (method) {
                ASMAPI.log("INFO", 'From Genshin Impact, redirecting EntityRenderDispatcher.getRenderer');

                for (var j = 0; j < method.instructions.size(); j++) {

                    var currentInstruction = method.instructions.get(j);

                    if (currentInstruction.getOpcode() === Opcodes.ARETURN) {

                        var list = new InsnList();
                        // loading second parameter from locals (entity)
                        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        // calling static method
                        list.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'com/gim/client/GenshinClientHooks',
                            'getRenderer',
                            '(Lnet/minecraft/client/renderer/entity/EntityRenderer;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;',
                            false
                        ));

                        // inserting instructions before ARETURN
                        method.instructions.insertBefore(currentInstruction, list);
                        return method;
                    }
                }

                return method;
            }
        }
    }
}