var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

function initializeCoreMod() {
    return {
        'getModel': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.LivingEntityRenderer'
            },
            'transformer': function (classNode) {
                ASMAPI.redirectFieldToMethod(classNode, ASMAPI.mapField('f_115290_'), ASMAPI.mapMethod('m_7200_')) // model
                return classNode;
            }
        }
    };
}