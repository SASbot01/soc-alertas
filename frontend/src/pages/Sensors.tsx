import React, { useEffect, useState } from 'react';
import { Radio, SignalHigh, CircleOff, RefreshCw } from 'lucide-react';
import { sensorService } from '../lib/services';
import type { Sensor } from '../types';
import EmptyState from '../components/EmptyState';

const Sensors: React.FC = () => {
    const [sensors, setSensors] = useState<Sensor[]>([]);
    const [loading, setLoading] = useState(true);

    const fetchSensors = async () => {
        setLoading(true);
        try {
            const data = await sensorService.list();
            setSensors(data);
        } catch (err) {
            console.error('Failed to fetch sensors:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchSensors(); }, []);

    if (loading) {
        return (
            <div className="space-y-6">
                <h1 className="text-2xl font-bold text-white">Network Sensors</h1>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {[...Array(3)].map((_, i) => (
                        <div key={i} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 animate-pulse">
                            <div className="h-12 w-12 bg-slate-700 rounded-xl mb-6" />
                            <div className="space-y-3">
                                <div className="h-4 bg-slate-700 rounded w-3/4" />
                                <div className="h-4 bg-slate-700 rounded w-1/2" />
                                <div className="h-4 bg-slate-700 rounded w-2/3" />
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-white">Network Sensors</h1>
                <button onClick={fetchSensors} className="flex items-center gap-2 px-3 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-sm text-slate-300 transition-colors">
                    <RefreshCw className="w-4 h-4" /> Refresh
                </button>
            </div>

            {sensors.length === 0 ? (
                <EmptyState icon={Radio} title="No sensors registered" message="Sensors will appear here once they send data via the upload endpoint." />
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {sensors.map((sensor) => (
                        <div key={sensor.id} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-6 relative overflow-hidden group hover:border-primary-500/30 transition-all">
                            <div className="absolute top-0 right-0 p-4">
                                {sensor.status === 'online' ? (
                                    <SignalHigh className="w-5 h-5 text-green-500" />
                                ) : (
                                    <CircleOff className="w-5 h-5 text-slate-600" />
                                )}
                            </div>

                            <div className="flex items-center gap-4 mb-6">
                                <div className="w-12 h-12 rounded-xl bg-slate-900 flex items-center justify-center border border-slate-700">
                                    <Radio className="w-6 h-6 text-primary-400" />
                                </div>
                                <div>
                                    <h3 className="text-lg font-semibold text-white">{sensor.name || sensor.id}</h3>
                                    <div className="text-xs text-slate-500 font-mono">{sensor.id}</div>
                                </div>
                            </div>

                            <div className="space-y-3">
                                <div className="flex justify-between text-sm">
                                    <span className="text-slate-400">Status</span>
                                    <span className={`${sensor.status === 'online' ? 'text-green-400' : 'text-slate-500'} font-medium capitalize`}>
                                        {sensor.status}
                                    </span>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-slate-400">Threats Found</span>
                                    <span className="text-white font-medium">{sensor.threatsDetected}</span>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-slate-400">Packets</span>
                                    <span className="text-white font-medium">{((sensor.packetsProcessed || 0) / 1000).toFixed(1)}k</span>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-slate-400">Last Seen</span>
                                    <span className="text-slate-500">{sensor.lastSeen ? new Date(sensor.lastSeen).toLocaleString() : 'Never'}</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Sensors;
