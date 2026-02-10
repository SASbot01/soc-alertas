import React, { useEffect, useState, useCallback } from 'react';
import { AlertTriangle, AlertOctagon, Info } from 'lucide-react';
import { threatService } from '../lib/services';
import type { ThreatEvent, ThreatListResponse } from '../types';
import DataTable, { type Column } from '../components/DataTable';
import FilterBar, { type FilterConfig } from '../components/FilterBar';
import StatusBadge from '../components/StatusBadge';
import EmptyState from '../components/EmptyState';

const Threats: React.FC = () => {
    const [data, setData] = useState<ThreatListResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [filters, setFilters] = useState<Record<string, string>>({});
    const [search, setSearch] = useState('');

    const fetchThreats = useCallback(async () => {
        setLoading(true);
        try {
            const params: Record<string, string | number> = { page, size: 20 };
            if (filters.status) params.status = filters.status;
            if (filters.minSeverity) params.minSeverity = filters.minSeverity;
            if (search) params.search = search;
            const result = await threatService.list(params);
            setData(result);
        } catch (err) {
            console.error('Failed to fetch threats:', err);
        } finally {
            setLoading(false);
        }
    }, [page, filters, search]);

    useEffect(() => { fetchThreats(); }, [fetchThreats]);

    const filterConfigs: FilterConfig[] = [
        {
            key: 'status', label: 'All Statuses',
            options: [
                { label: 'Detected', value: 'detected' },
                { label: 'Blocked', value: 'blocked' },
                { label: 'Resolved', value: 'resolved' },
            ]
        },
        {
            key: 'minSeverity', label: 'Min Severity',
            options: [
                { label: '3+', value: '3' },
                { label: '5+', value: '5' },
                { label: '7+', value: '7' },
            ]
        },
    ];

    const getSeverityIcon = (severity: number) => {
        if (severity >= 5) return <AlertOctagon className="w-5 h-5 text-red-500" />;
        if (severity >= 3) return <AlertTriangle className="w-5 h-5 text-yellow-500" />;
        return <Info className="w-5 h-5 text-blue-500" />;
    };

    const columns: Column<ThreatEvent>[] = [
        {
            key: 'threatType', header: 'Type',
            render: (t) => (
                <div className="flex items-center gap-3">
                    {getSeverityIcon(t.severity)}
                    <span className="font-medium text-white">{t.threatType}</span>
                </div>
            )
        },
        {
            key: 'severity', header: 'Severity',
            render: (t) => (
                <span className={`px-2 py-1 rounded text-xs font-bold ${
                    t.severity >= 5 ? 'bg-red-500/20 text-red-400' :
                    t.severity >= 3 ? 'bg-yellow-500/20 text-yellow-400' :
                    'bg-blue-500/20 text-blue-400'
                }`}>
                    Level {t.severity}
                </span>
            )
        },
        { key: 'srcIp', header: 'Source IP', render: (t) => <span className="font-mono text-slate-300">{t.srcIp}</span> },
        { key: 'dstIp', header: 'Target', render: (t) => <span className="font-mono text-slate-300">{t.dstIp}</span> },
        {
            key: 'status', header: 'Status',
            render: (t) => <StatusBadge status={t.status} />
        },
        {
            key: 'timestamp', header: 'Time',
            render: (t) => <span className="text-slate-400">{new Date(t.timestamp).toLocaleString()}</span>
        },
    ];

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-white">Threat Intelligence</h1>

            <FilterBar
                filters={filterConfigs}
                values={filters}
                onFilterChange={(key, value) => { setFilters(prev => ({ ...prev, [key]: value })); setPage(0); }}
                searchValue={search}
                onSearchChange={(val) => { setSearch(val); setPage(0); }}
                searchPlaceholder="Search by IP..."
            />

            {!loading && data && data.content.length === 0 ? (
                <EmptyState title="No threats found" message="No threat events match your current filters." />
            ) : (
                <DataTable
                    columns={columns}
                    data={data?.content || []}
                    loading={loading}
                    page={page}
                    totalPages={data?.totalPages}
                    onPageChange={setPage}
                    keyExtractor={(t) => t.id}
                />
            )}
        </div>
    );
};

export default Threats;
